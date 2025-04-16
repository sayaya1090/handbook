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
internal class TypeVersionOverlapTest(
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
    @Suppress("UNCHECKED_CAST")
    fun dumpType(): List<String> {
        val result: List<Type> = em.createNativeQuery("SELECT * FROM type t", Type::class.java).resultList as List<Type>
        return result.map { "${ it.id } ${ it.name } ${ it.version } -> ${ it.effectDateTime } ~ ${ it.expireDateTime } ${ it.last }" }
    }
    Given("단일 워크스페이스에서") {
        val workspace = UUID.randomUUID()
        When("겹치지 않는 기간의 데이터가 삽입되면") {
            val typeVersion1 = Type.of(
                workspace = workspace,
                user = user,
                type = "type_1", parent=null,
                version = "1.0",
                effectDateTime = Instant.parse("2025-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-12-31T23:59:59Z")
            )
            val typeVersion2 = Type.of(
                workspace = workspace,
                user = user,
                type = "type_1", parent=null,
                version = "2.0",
                effectDateTime = Instant.parse("2024-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2024-12-31T23:59:59Z")
            )

            tx.transactional {
                em.persist(typeVersion1)
                em.persist(typeVersion2)
            }

            Then("두 데이터가 정상 저장된다") {
                val results = em.createNativeQuery ("SELECT * FROM Type t WHERE workspace='$workspace'::uuid AND t.name = :type AND t.last = true", Type::class.java)
                    .setParameter("type", "type_1")
                    .resultList
                results.size shouldBe 2
            }
            println(dumpType())
        }

        When("겹치는 기간의 데이터를 삽입하면") {
            val overlappingTypeVersion = Type.of(
                workspace = workspace,
                user = user,
                type = "type_1", parent=null,
                version = "3.0",
                effectDateTime = Instant.parse("2025-06-01T00:00:00Z"),
                expireDateTime = Instant.parse("2025-12-01T23:59:59Z")
            )
            Then("트리거에 의해 예외가 발생해야 한다") {
                val exception = shouldThrow<SQLException> {
                    tx.transactional {
                        em.persist(overlappingTypeVersion)
                    }
                }
                exception.message shouldStartWith "ERROR: Overlapping periods are not allowed for type: type_1, effective_at:" // 메시지 검증
            }
        }

        When("기간이 겹치도록 데이터가 업데이트 되면") {
            Then("트리거에 의해 예외가 발생해야 한다") {
                val exception = shouldThrow<SQLException> {
                    tx.transactional {
                        val typeVersionToUpdate: Type = em.createQuery("SELECT t FROM Type t WHERE t.name = :type AND t.version = :version", Type::class.java)
                            .setParameter("type", "type_1")
                            .setParameter("version", "2.0")
                            .singleResult
                        typeVersionToUpdate.effectDateTime = Instant.parse("2025-06-01T00:00:00Z")
                        typeVersionToUpdate.expireDateTime = Instant.parse("2025-12-01T23:59:59Z")
                        em.merge(typeVersionToUpdate)  // 기간이 오버랩되도록 데이터 변경
                    }
                }
                exception.message shouldStartWith "ERROR: Overlapping periods are not allowed for type: type_1, effective_at:"
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
            while (cause.cause != null && cause != cause.cause) { // 자신과 부모가 동일하지 않을 때
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