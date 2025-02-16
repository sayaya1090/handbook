package dev.sayaya.handbook.entity

import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.time.Instant

/*
  Type 추가, 변경, 삭제 시 TypeHierarchyClosure 테이블에 정의된 트리거 작동을 테스트한다
 */
@SpringBootTest(properties = [
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=update"
])
internal class TypeHierarchyClosureTest (
    private val tx: PlatformTransactionManager,
    @PersistenceContext private val em: EntityManager
) : BehaviorSpec({
    Given("DB 초기화") {
        val user = User().apply {
            id = "system"
            name = "system"
            createDateTime = Instant.now()
            lastModifyDateTime = Instant.now()
        }
        tx.transactional {
            ClassPathResource("createTriggers.sql").let { em.execute(it) }  // 트리거 생성
            ClassPathResource("createMaterializedView.sql").let { em.execute(it) }  // MV 생성
            em.merge(user)
        }
        When("계층 구조를 가진 데이터를 저장하면") {
            var type1 = Type.of(user, "type_1", null)
            var type2 = Type.of(user, "type_2", type1)
            var type3 = Type.of(user, "type_3", type2)

            tx.transactional {
                type1 = em.merge(type1)
                type2 = em.merge(type2)
                type3 = em.merge(type3)
            }
            @Suppress("UNCHECKED_CAST")
            fun dumpTypeHierarchyClosure(): List<String> {
                val result: List<TypeHierarchyClosure> = em.createNativeQuery(
                    """
                    SELECT * FROM type_hierarchy_closure t
                    WHERE t.descendant IN ('type_1', 'type_2', 'type_3')
                    ORDER BY t.descendant, t.depth
                    """.trimIndent(),
                    TypeHierarchyClosure::class.java
                ).resultList as List<TypeHierarchyClosure>
                return result.map { "${ it.descendant.id } -> ${ it.ancestor.id } (depth=${ it.depth })" }
            }
            Then("TypeHierarchyCloser 테이블에 전체 계층 구조가 저장된다") {
                val mappedResult = dumpTypeHierarchyClosure()
                val expectedResult = listOf(
                    "type_1 -> type_1 (depth=0)",
                    "type_2 -> type_2 (depth=0)",
                    "type_2 -> type_1 (depth=1)",
                    "type_3 -> type_3 (depth=0)",
                    "type_3 -> type_2 (depth=1)",
                    "type_3 -> type_1 (depth=2)"
                )
                mappedResult shouldContainAll expectedResult
                mappedResult.size shouldBe expectedResult.size
            }
            When("계층 구조가 변경되면") {
                type3.parent = type1
                tx.transactional {
                    em.merge(type3)
                }
                Then("TypeHierarchyCloser 테이블에 전체 계층 구조가 저장된다") {
                    val mappedResult = dumpTypeHierarchyClosure()
                    val expectedResult = listOf(
                        "type_1 -> type_1 (depth=0)",
                        "type_2 -> type_2 (depth=0)",
                        "type_2 -> type_1 (depth=1)",
                        "type_3 -> type_3 (depth=0)",
                        "type_3 -> type_1 (depth=1)",
                    )
                    mappedResult shouldContainAll expectedResult
                    mappedResult.size shouldBe expectedResult.size
                }
            }
            When("리프 노드를 삭제하면") {
                tx.transactional {
                    em.find(Type::class.java, type3.id).let(em::remove)
                }
                Then("TypeHierarchyCloser 테이블에 삭제 후 전체 계층 구조가 반영된다") {
                    val mappedResult = dumpTypeHierarchyClosure()
                    val expectedResult = listOf(
                        "type_1 -> type_1 (depth=0)",
                        "type_2 -> type_2 (depth=0)",
                        "type_2 -> type_1 (depth=1)"
                    )
                    mappedResult shouldContainAll expectedResult
                    mappedResult.size shouldBe expectedResult.size
                }
            }
            When("부모 노드를 삭제하면") {
                tx.transactional {
                    em.find(Type::class.java, type1.id).let(em::remove)
                }
                Then("TypeHierarchyCloser 테이블에 삭제 후 전체 계층 구조가 반영된다") {
                    val mappedResult = dumpTypeHierarchyClosure()
                    mappedResult shouldBe emptyList()
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
            } catch (e: Exception) {
                rollback(status)
                throw e
            }
        }
        fun EntityManager.execute(resource: ClassPathResource) {
            resource.inputStream.bufferedReader().use { it.readText() }.trimIndent().let { execute(it) }
        }
        fun EntityManager.execute(sql: String) {
            sql.also(::println)
                .let(::createNativeQuery)
                .executeUpdate()
        }
    }
}
