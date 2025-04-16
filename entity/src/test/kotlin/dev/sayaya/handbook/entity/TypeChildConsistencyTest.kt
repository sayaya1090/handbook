package dev.sayaya.handbook.entity

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
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
internal class TypeChildConsistencyTest(
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
        em.merge(user)
    }
    Given("단일 워크스페이스에서") {
        val workspace = UUID.randomUUID()
        When("삭제 시도를 할 때 부모와 자식의 유효기간이 겹치는 경우") {
            val parentType = Type.of(
                workspace = workspace,
                user = user,
                type = "conflict_parent",
                version = "1.0", parent = null,
                effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-06-30T23:59:59Z")
            )
            val parentType2 = Type.of(
                workspace = workspace,
                user = user,
                type = "conflict_parent",
                version = "2.0", parent = null,
                effectDateTime = Instant.parse("2025-06-30T23:59:59Z"),
                expireDateTime = Instant.parse("2025-12-31T23:59:59Z")
            )
            val childType = Type.of(
                workspace = workspace,
                user = user,
                type = "conflict_child",
                version = "1.0", parent = "conflict_parent",
                effectDateTime = Instant.parse("2025-03-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-08-30T23:59:59Z")
            )
            tx.transactional {
                em.persist(parentType)
                em.persist(parentType2)
                em.persist(childType)
            }

            Then("부모를 삭제하면 예외 발생") {
                val exception = shouldThrow<SQLException> {
                    tx.transactional {
                        em.createNativeQuery("DELETE FROM type WHERE workspace='$workspace'::uuid AND name='${parentType.name}' AND version='${parentType.version}'").executeUpdate()
                    }
                }
                exception.message shouldStartWith "ERROR: Cannot delete parent type (name=conflict_parent, version=1.0) as it still has associated children during the period"
            }
        }
        When("삭제하려는 부모의 기간 내 자식 데이터가 없는 경우") {
            val parentType = Type.of(
                workspace = workspace,
                user = user,
                type = "parent",
                version = "1.0", parent = null,
                effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-06-30T23:59:59Z")
            )
            val childType = Type.of(
                workspace = workspace,
                user = user,
                type = "child",
                version = "1.0", parent = "parent",
                effectDateTime = Instant.parse("2025-03-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-05-30T23:59:59Z")
            )
            val parentTypeNonOverlap = Type.of(
                workspace = workspace,
                user = user,
                type = "parent",
                version = "2.0", parent = null,
                effectDateTime = Instant.parse("2025-06-30T23:59:59Z"),
                expireDateTime = Instant.parse("2025-07-30T23:59:59Z")
            )
            tx.transactional {
                em.persist(parentType)
                em.persist(parentTypeNonOverlap)
                em.persist(childType)
            }
            val inserted = em.createNativeQuery("SELECT * FROM Type t WHERE workspace='$workspace'::uuid AND t.name = :type", Type::class.java)
                .setParameter("type", "parent")
                .resultList
            inserted.size shouldBe 2

            Then("부모 삭제가 성공해야 한다") {
                tx.transactional {
                    em.createNativeQuery("DELETE FROM type WHERE workspace='$workspace'::uuid AND name='${parentTypeNonOverlap.name}' AND version='${parentTypeNonOverlap.version}'").executeUpdate()
                }
                val remaining = em.createNativeQuery("SELECT * FROM Type t WHERE workspace='$workspace'::uuid AND t.name = :type", Type::class.java)
                    .setParameter("type", "parent")
                    .resultList
                remaining.size shouldBe 1
            }
        }
        When("부모 타입의 유효기간을 업데이트할 때") {
            val parentType = Type.of(
                workspace = workspace,
                user = user,
                type = "update_parent",
                version = "1.0", parent = null,
                effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-12-31T23:59:59Z")
            )
            val childType = Type.of(
                workspace = workspace,
                user = user,
                type = "update_child",
                version = "1.0", parent = "update_parent",
                effectDateTime = Instant.parse("2025-03-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-08-31T23:59:59Z")
            )

            tx.transactional {
                em.persist(parentType)
                em.persist(childType)
            }

            Then("부모의 새 유효기간이 자식과의 겹치는 구간을 완전히 포함하지 않으면 예외 발생") {
                // 자식과 겹치는 구간(2025-03-01 ~ 2025-08-31)을 벗어나는 업데이트 시도
                val exception = shouldThrow<SQLException> {
                    tx.transactional { em.createNativeQuery("""
                    UPDATE type 
                    SET effective_at = '2025-03-01T00:00:00Z',
                        expire_at = '2025-07-31T23:59:59Z'
                    WHERE name = 'update_parent' AND version = '1.0'
                """.trimIndent()).executeUpdate()
                    }
                }
                exception.message shouldStartWith "ERROR: Cannot modify parent type (name=update_parent) as the new effective period"
            }

            Then("부모의 새 유효기간이 자식과의 겹치는 구간을 포함하면 성공") {
                tx.transactional {
                    val result = em.createNativeQuery("""
                UPDATE type 
                SET effective_at = '2025-02-01T00:00:00Z',
                    expire_at = '2025-09-30T23:59:59Z'
                WHERE name = 'update_parent' AND version = '1.0'
            """.trimIndent()).executeUpdate()

                    result shouldBe 1
                }
            }
        }
    }
}) {
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