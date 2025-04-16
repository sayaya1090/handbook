package dev.sayaya.handbook.entity

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.sql.SQLException
import java.time.Instant
import java.util.*

@SpringBootTest(properties = [
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.sql.init.schema-locations=classpath:createTable.sql",
    "spring.sql.init.mode=always"
])
internal class TypeWorkspaceIsolationTest(
    private val tx: PlatformTransactionManager,
    @PersistenceContext private val em: EntityManager
) : BehaviorSpec({
    val user = User().apply {
        id = UUID.fromString("93951bc3-be1e-4fc8-865f-d6376ac3e87b")
        name = "system"
        createDateTime = Instant.now()
        lastModifyDateTime = Instant.now()
        provider = "handbook"
        account = "system"
    }

    tx.transactional {
        ClassPathResource("createTriggers.sql").let { em.execute(it) }  // 트리거 생성
        ClassPathResource("createMaterializedView.sql").let { em.execute(it) }  // MV 생성
        em.merge(user)
    }
    Given("서로 다른 워크스페이스에서") {
        val workspace1 = UUID.randomUUID()
        val workspace2 = UUID.randomUUID()
        When("동일한 이름의 타입을 저장하면") {
            val parentType1 = Type.of(
                workspace = workspace1,
                user = user,
                type = "same_type_name",
                version = "1.0", parent = null,
                effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-06-30T23:59:59Z")
            )

            val parentType2 = Type.of(
                workspace = workspace2,
                user = user,
                type = "same_type_name", // 동일한 이름
                version = "1.0", parent = null,   // 동일한 버전
                effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-06-30T23:59:59Z")
            )
            parentType1.workspace shouldNotBe parentType2.workspace
            parentType1.name shouldBe parentType2.name
            parentType1.version shouldBe parentType2.version

            Then("저장되어야 함") {
                tx.transactional {
                    em.persist(parentType1)
                    em.persist(parentType2)
                }
                em.createQuery("SELECT t FROM Type t WHERE t.name = :type AND version = :version", Type::class.java)
                  .setParameter("type", parentType1.name)
                  .setParameter("version", parentType1.version)
                  .resultList.size shouldBe 2
            }
            When("워크스페이스1의 타입 정보를 변경하면") {
                tx.transactional {
                    val updateResult = em.createNativeQuery("""
                        UPDATE type 
                        SET expire_at = '2025-07-31T23:59:59Z'
                        WHERE workspace=:workspace AND name=:type AND version=:version
                    """).setParameter("workspace", workspace1)
                        .setParameter("type", parentType1.name)
                        .setParameter("version", parentType1.version)
                        .executeUpdate()
                    updateResult shouldBe 1
                }
                Then("워크스페이스1에는 반영되어야 함") {
                    val parentInWorkspace1After = em.createNativeQuery(
                        "SELECT * FROM Type t WHERE workspace=:workspace AND name=:type AND version=:version",
                        Type::class.java
                    ).setParameter("workspace", workspace1)
                     .setParameter("type", parentType1.name)
                     .setParameter("version", parentType1.version).resultList.first() as Type
                    parentInWorkspace1After.expireDateTime.toString() shouldBe "2025-07-31T23:59:59Z"

                }
                Then("워크스페이스2의 데이터에는 영향이 없어야 함") {
                    val parentInWorkspace2After = em.createNativeQuery(
                        "SELECT * FROM Type t WHERE workspace=:workspace AND name=:type AND version=:version",
                        Type::class.java
                    ).setParameter("workspace", workspace2)
                     .setParameter("type", parentType1.name)
                     .setParameter("version", parentType1.version).resultList.first() as Type
                    parentInWorkspace2After.expireDateTime.toString() shouldBe "2025-06-30T23:59:59Z"
                }
            }
            When("워크스페이스1의 데이터를 삭제하면") {
                tx.transactional {
                    em.createNativeQuery(
                        "DELETE FROM type WHERE workspace=:workspace AND name=:type AND version=:version"
                    ).setParameter("workspace", workspace1)
                     .setParameter("type", parentType1.name)
                     .setParameter("version", parentType1.version)
                     .executeUpdate()
                }
                Then("워크스페이스1의 데이터는 삭제되어야 함") {
                    em.createNativeQuery(
                        "SELECT * FROM Type t WHERE workspace=:workspace AND name=:type AND version=:version",
                        Type::class.java
                    ).setParameter("workspace", workspace1)
                     .setParameter("type", parentType1.name)
                     .setParameter("version", parentType1.version)
                     .resultList.size shouldBe 0  // 삭제됨
                }
                Then("워크스페이스2의 데이터에는 영향이 없어야 함") {
                    em.createNativeQuery(
                        "SELECT * FROM Type t WHERE workspace=:workspace AND name=:type AND version=:version",
                        Type::class.java
                    ).setParameter("workspace", workspace2)
                     .setParameter("type", parentType1.name)
                     .setParameter("version", parentType1.version)
                     .resultList.size shouldBe 1  // 여전히 존재
                }
            }
        }
        When("다른 워크스페이스의 부모를 참조하는 자식 타입 생성 시") {
            val parentTypeInWorkspace1 = Type.of(
                workspace = workspace1,
                user = user,
                type = "parent_type_from_workspace1",
                version = "1.0", parent = null,
                effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-12-31T23:59:59Z")
            )

            tx.transactional {
                em.persist(parentTypeInWorkspace1)
            }

            val childTypeInWorkspace2 = Type.of(
                workspace = workspace2,
                user = user,
                type = "cross_workspace_child",
                version = "1.0", parent = "parent_type_from_workspace1",
                effectDateTime = Instant.parse("2025-03-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-09-30T23:59:59Z")
            )

            childTypeInWorkspace2.parent shouldBe parentTypeInWorkspace1.name

            Then("예외가 발생해야 함") {
                val exception = shouldThrow<SQLException> {
                    tx.transactional {
                        em.persist(childTypeInWorkspace2)
                    }
                }
                exception.message shouldStartWith "ERROR: Parent type (name=${parentTypeInWorkspace1.name}) does not exist"
            }
        }
    }
}){
    companion object {
        private val database = Database()

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            database.registerDynamicProperties(registry)
        }

        fun PlatformTransactionManager.transactional(action: () -> Unit) {
            val transactionDefinition = DefaultTransactionDefinition()
            val status = getTransaction(transactionDefinition)
            try {
                action()
                commit(status)
            } catch (e: JpaSystemException) {
                if (!status.isCompleted) rollback(status)
                throw unwrapException(e)
            } catch (e: Exception) {
                if (!status.isCompleted) rollback(status)
                throw e
            }
        }

        private fun unwrapException(exception: JpaSystemException): Throwable {
            var cause: Throwable = exception
            while (cause.cause != null && cause != cause.cause) {
                cause = cause.cause!!
            }
            return cause
        }

        private fun EntityManager.execute(resource: ClassPathResource) {
            resource.inputStream.bufferedReader().use { it.readText() }.trimIndent().let { execute(it) }
        }

        private fun EntityManager.execute(sql: String) {
            sql.also(::println)
                .let(::createNativeQuery)
                .executeUpdate()
        }
    }
}