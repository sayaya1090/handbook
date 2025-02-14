package dev.sayaya.handbook.entity.view

import dev.sayaya.handbook.entity.*
import dev.sayaya.handbook.entity.Type
import dev.sayaya.handbook.entity.User
import dev.sayaya.handbook.entity.attributes.ArrayAttribute
import dev.sayaya.handbook.entity.attributes.DocumentAttribute
import dev.sayaya.handbook.entity.attributes.ValueAttribute
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
  Type 추가, 변경, 삭제 시 자식 Type이 부모 Type의 Attribute를 상속하는지 검증한다
 */
@SpringBootTest(properties = [
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=update"
])
internal class TypeAttributesMvTest(
    val tx: PlatformTransactionManager,
    @PersistenceContext private val em: EntityManager
) : BehaviorSpec({
    Given("DB 초기화") {
        val user = User().apply {
            id = "system"
            name = "system"
            createDateTime = Instant.now()
            lastModifyDateTime = Instant.now()
        }
        var type1 = Type.of(user,"type_1", null)
        var type2 = Type.of(user,"type_2", type1)
        var type3 = Type.of(user,"type_3", type2)
        tx.transactional {
            ClassPathResource("createTriggers.sql").let { em.execute(it) }  // 트리거 생성
            ClassPathResource("createMaterializedView.sql").let { em.execute(it) }  // MV 생성
            em.merge(user)
            em.merge(type1)
            em.merge(type2)
            em.merge(type3)
        }
        When("계층 구조를 가진 데이터에 Attribute를 저장하고 MV를 업데이트하면") {
            tx.transactional {
                type1 = em.find(Type::class.java, type1.id)
                type2 = em.find(Type::class.java, type2.id)
                type3 = em.find(Type::class.java, type3.id)
                val type1Attr1 = ValueAttribute.of(em.find(Type::class.java, type1.id), "common_attr").apply {
                    description = "Common Attribute in Type1"
                }
                val type2Attr1 = ArrayAttribute.of(type2, "unique_attr", AttributeType.Value).apply {
                    description = "Unique Attribute in Type2"
                }
                val type2Attr2 = ValueAttribute.of(type2, "common_attr").apply {
                    description = "Overwritten Attribute in Type2"
                }
                val type3Attr1 = DocumentAttribute.of(type3, "exclusive_attr", type1).apply {
                    description = "Exclusive Attribute in Type3"
                }
                em.merge(type1Attr1)
                em.merge(type2Attr1)
                em.merge(type2Attr2)
                em.merge(type3Attr1)
                em.createNativeQuery("REFRESH MATERIALIZED VIEW type_attributes").executeUpdate()
            }
            @Suppress("UNCHECKED_CAST")
            fun dumpTypeAttributes(): List<String> {
                val result: List<TypeAttributesMv> = em.createNativeQuery(
                    """
                    SELECT * FROM type_attributes t
                    ORDER BY t.type, t.name
                    """.trimIndent(),
                    TypeAttributesMv::class.java
                ).resultList as List<TypeAttributesMv>
                return result.map { "${it.type}:${it.name} (description=${it.description})" }
            }
            Then("TypeHierarchyCloser 테이블에 전체 계층 구조가 저장된다") {
                val mappedResult = dumpTypeAttributes()
                val expectedResult = listOf(
                    "type_1:common_attr (description=Common Attribute in Type1)",
                    "type_2:common_attr (description=Overwritten Attribute in Type2)",
                    "type_2:unique_attr (description=Unique Attribute in Type2)",
                    "type_3:common_attr (description=Overwritten Attribute in Type2)",
                    "type_3:exclusive_attr (description=Exclusive Attribute in Type3)",
                    "type_3:unique_attr (description=Unique Attribute in Type2)"
                )
                mappedResult shouldContainAll expectedResult
                mappedResult.size shouldBe expectedResult.size
            }
            When("계층 구조를 변경하고 MV를 업데이트하면") {
                tx.transactional {
                    em.merge(type3.apply {
                        parent = type1
                    }) // Type 계층 구조 변경
                    em.createNativeQuery("DELETE FROM attribute WHERE type='${type2.id}' AND name='common_attr'").executeUpdate() // 상속하던 속성 삭제
                    em.createNativeQuery("REFRESH MATERIALIZED VIEW type_attributes").executeUpdate()
                }
                Then("TypeHierarchyCloser 테이블에 변경된 계층 구조가 저장된다") {
                    val mappedResult = dumpTypeAttributes()
                    val expectedResult = listOf(
                        "type_1:common_attr (description=Common Attribute in Type1)",
                        "type_2:common_attr (description=Common Attribute in Type1)",   // 오버라이드 속성이 제거되고 부모를 상속한다
                        "type_2:unique_attr (description=Unique Attribute in Type2)",
                        "type_3:common_attr (description=Common Attribute in Type1)",
                        "type_3:exclusive_attr (description=Exclusive Attribute in Type3)",
                        // 부모가 변경되어 type_3의 type_2 상속 속성이 제거된다
                    )
                    mappedResult shouldContainAll expectedResult
                    mappedResult.size shouldBe expectedResult.size
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