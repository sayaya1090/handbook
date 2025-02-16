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

@SpringBootTest(properties = [
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=update"
])
internal class TypeParentConsistencyTest(
    private val tx: PlatformTransactionManager,
    @PersistenceContext private val em: EntityManager
) : BehaviorSpec({
    Given("DB 초기화와 트리거 설정") {
        val user = User().apply {
            id = "system"
            name = "system"
            createDateTime = Instant.now()
            lastModifyDateTime = Instant.now()
        }

        tx.transactional {
            ClassPathResource("createTriggers.sql").let { em.execute(it) }  // 트리거 생성
            em.merge(user)
        }

        When("부모 타입이 없는 상태에서 자식 데이터를 삽입하면") {
            val childType = Type.of(
                user = user,
                type = "child_type_1", parent = "nonexistent_parent",
                version = "1.0",
                effectDateTime = Instant.parse("2023-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2023-12-31T23:59:59Z")
            )
            Then("부모 타입이 없으므로 예외가 발생해야 한다") {
                val exception = shouldThrow<SQLException> {
                    tx.transactional {
                        em.persist(childType)
                    }
                }
                exception.message shouldStartWith "ERROR: Parent type (name=nonexistent_parent) does not exist"
            }
        }

        When("부모 타입들의 기간에 gap이 존재하는 경우") {
            val parentType1 = Type.of(
                user = user,
                type = "parent_type_gapped", parent = null,
                version = "1.0",
                effectDateTime = Instant.parse("2023-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2023-06-30T23:59:59Z")
            )
            val parentType2 = Type.of(
                user = user,
                type = "parent_type_gapped", parent = null,
                version = "2.0",
                effectDateTime = Instant.parse("2023-07-15T00:00:00Z"), // 기간 중간에 gap이 존재
                expireDateTime = Instant.parse("2023-12-31T23:59:59Z")
            )
            val childType = Type.of(
                user = user,
                type = "child_type_2", parent = "parent_type_gapped",
                version = "1.0",
                effectDateTime = Instant.parse("2023-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2023-12-31T23:59:59Z")
            )
            tx.transactional {
                em.persist(parentType1)
                em.persist(parentType2)
            }

            Then("gap으로 인해 자식 데이터 삽입이 실패해야 한다") {
                val exception = shouldThrow<SQLException> {
                    tx.transactional {
                        em.persist(childType)
                    }
                }
                exception.message shouldStartWith "ERROR: Parent type (name=parent_type_gapped) is missing or has gaps or does not fully cover the period [2023-01-01 09:00:00+09, 2024-01-01 08:59:59+09]"
            }
        }

        When("부모 유효 기간이 자식 데이터를 완전히 커버하지 않는 경우") {
            val parentType = Type.of(
                user = user,
                type = "parent_type_incomplete", parent = null,
                version = "1.0",
                effectDateTime = Instant.parse("2023-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2023-06-30T23:59:59Z")
            )
            val childType = Type.of(
                user = user,
                type = "child_type_3", parent = "parent_type_incomplete",
                version = "1.0",
                effectDateTime = Instant.parse("2023-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2023-12-31T23:59:59Z") // 자식이 부모 기간을 초과함
            )
            tx.transactional {
                em.persist(parentType)
            }

            Then("부모 데이터가 자식을 완벽히 포함하지 않아 삽입이 실패해야 한다") {
                val exception = shouldThrow<SQLException> {
                    tx.transactional {
                        em.persist(childType)
                    }
                }
                exception.message shouldStartWith "ERROR: Parent type (name=parent_type_incomplete) is missing or has gaps or does not fully cover the period [2023-01-01 09:00:00+09, 2024-01-01 08:59:59+09]"
            }
        }

        When("유효한 부모 데이터가 삽입되고 자식 데이터도 유효한 경우") {
            val validParentType = Type.of(
                user = user,
                type = "parent_type_valid", parent = null,
                version = "1.0",
                effectDateTime = Instant.parse("2023-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2023-12-31T23:59:59Z")
            )
            val validChildType = Type.of(
                user = user,
                type = "child_type_4", parent = "parent_type_valid",
                version = "1.0",
                effectDateTime = Instant.parse("2023-01-01T00:00:00Z"),
                expireDateTime = Instant.parse("2023-12-31T23:59:59Z")
            )
            tx.transactional {
                em.persist(validParentType)
            }

            Then("부모 데이터가 자식을 완벽히 포함하므로 삽입이 성공해야 한다") {
                tx.transactional {
                    em.persist(validChildType)
                }

                val results = em.createQuery("SELECT t FROM Type t WHERE t.name = :type", Type::class.java)
                    .setParameter("type", "child_type_4")
                    .resultList
                results.size shouldBe 1
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