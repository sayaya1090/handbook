package dev.sayaya.handbook.entity.view

import dev.sayaya.handbook.entity.*
import dev.sayaya.handbook.entity.attributes.*
import dev.sayaya.handbook.entity.attributes.ArrayAttribute
import dev.sayaya.handbook.entity.attributes.DocumentAttribute
import dev.sayaya.handbook.entity.attributes.FileAttribute
import dev.sayaya.handbook.entity.attributes.MapAttribute
import dev.sayaya.handbook.entity.attributes.ValueAttribute
import dev.sayaya.handbook.testcontainer.Database
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/*
  Type 추가, 변경, 삭제 시 자식 Type이 부모 Type의 Attribute를 상속하는지 검증한다
 */
@SpringBootTest(properties = [
    "spring.jpa.show-sql=true",
    "spring.jpa.hibernate.ddl-auto=update"
])
internal class TypeAttributesMvTest(
    val tx: PlatformTransactionManager,
    @PersistenceContext private val em: EntityManager,
) : BehaviorSpec({
    Given("DB 초기화") {
        var type1Version1: Type? = null
        var type2Version1: Type? = null
        var type2Version2: Type? = null
        var type3Version1: Type? = null
        var type3Version2: Type? = null

        tx.transactional {
            ClassPathResource("createTriggers.sql").let { em.execute(it) }  // 트리거 생성
            ClassPathResource("createMaterializedView.sql").let { em.execute(it) }  // MV 생성
            val dateMin = LocalDate.of(1970, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val dateMin2 = LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val dateMax = LocalDate.of(2999, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val user = em.merge(User().apply {
                id = "system"
                name = "system"
                createDateTime = Instant.now()
                lastModifyDateTime = Instant.now()
            })
            type1Version1 = em.merge(Type.of(UUID.randomUUID(), user,"type_1", "t1-v1", null, dateMin, dateMax))
            type2Version1 = em.merge(Type.of(UUID.randomUUID(), user,"type_2", "t2-v1", "type_1", dateMin2, LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
            type2Version2 = em.merge(Type.of(UUID.randomUUID(), user,"type_2", "t2-v2", "type_1", type2Version1!!.expireDateTime, dateMax))
            type3Version1 = em.merge(Type.of(UUID.randomUUID(), user,"type_3", "t3-v1", "type_2", dateMin2, type2Version1!!.expireDateTime))
            type3Version2 = em.merge(Type.of(UUID.randomUUID(), user,"type_3", "t3-v2", "type_2", type3Version1!!.expireDateTime, dateMax))

        }
        When("계층 구조를 가진 데이터에 Attribute를 저장하고 MV를 업데이트하면") {
            tx.transactional {
                val type1Def = em.find(Type::class.java, type1Version1!!.id)
                val type2Def = em.find(Type::class.java, type2Version1!!.id)
                val type2Def2 = em.find(Type::class.java, type2Version2!!.id)
                val type3Def = em.find(Type::class.java, type3Version1!!.id)
                val type3Def2 = em.find(Type::class.java, type3Version2!!.id)
                val type1DefAttr1 = ValueAttribute.of(type1Def, "common_attr").apply {
                    description = "Common Attribute in Type1"
                }
                val type2DefAttr1 = ArrayAttribute.of(type2Def, "unique_attr", AttributeType.Value).apply {
                    description = "Unique Attribute in Type2"
                }
                val type2DefAttr2 = ValueAttribute.of(type2Def, "common_attr").apply {
                    description = "Overwritten Attribute in Type2"
                }
                val type3DefAttr1 = DocumentAttribute.of(type3Def, "exclusive_attr", "type_1").apply {
                    description = "Exclusive Attribute in Type3"
                }
                val type2Def2Attr2 = FileAttribute.of(type2Def2, "common_attr", "png,jpg,jpeg").apply {
                    description = "Overwritten Attribute in Type2"
                }
                val type3Def2Attr1 = DocumentAttribute.of(type3Def2, "exclusive_attr", "type_1").apply {
                    description = "Exclusive Attribute in Type3"
                }
                val type3Def2Attr2 = MapAttribute.of(type3Def2, "exclusive_attr2", AttributeType.Value, AttributeType.Value).apply {
                    description = "Added Attribute in Type3"
                }
                em.merge(type1DefAttr1)
                em.merge(type2DefAttr1)
                em.merge(type2DefAttr2)
                em.merge(type3DefAttr1)
                em.merge(type2Def2Attr2)
                em.merge(type3Def2Attr1)
                em.merge(type3Def2Attr2)
                em.createNativeQuery("REFRESH MATERIALIZED VIEW type_attributes").executeUpdate()
            }
            @Suppress("UNCHECKED_CAST")
            fun dumpTypeAttributes(): List<String> {
                val result: List<TypeAttributesMv> = em.createNativeQuery(
                    """
                    SELECT * FROM type_attributes t
                    ORDER BY t.type, t.effective_at, t.name
                    """.trimIndent(),
                    TypeAttributesMv::class.java
                ).resultList as List<TypeAttributesMv>
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
                return result.map { "${it.type}:${it.name} [${formatter.format(it.effectiveDateTime)}~${formatter.format(it.expiryDateTime)}] (description=${it.description})" }
            }
            Then("TypeHierarchyCloser 테이블에 전체 계층 구조가 저장된다") {
                val mappedResult = dumpTypeAttributes()
                val expectedResult = listOf(
                    "type_1:common_attr [1970-01-01~2999-12-31] (description=Common Attribute in Type1)",
                    "type_2:common_attr [1990-01-01~2000-01-01] (description=Overwritten Attribute in Type2)",
                    "type_2:unique_attr [1990-01-01~2000-01-01] (description=Unique Attribute in Type2)",
                    "type_2:common_attr [2000-01-01~2999-12-31] (description=Overwritten Attribute in Type2)",
                    "type_3:common_attr [1990-01-01~2000-01-01] (description=Overwritten Attribute in Type2)",
                    "type_3:exclusive_attr [1990-01-01~2000-01-01] (description=Exclusive Attribute in Type3)",
                    "type_3:unique_attr [1990-01-01~2000-01-01] (description=Unique Attribute in Type2)",
                    "type_3:common_attr [2000-01-01~2999-12-31] (description=Overwritten Attribute in Type2)",
                    "type_3:exclusive_attr [2000-01-01~2999-12-31] (description=Exclusive Attribute in Type3)",
                    "type_3:exclusive_attr2 [2000-01-01~2999-12-31] (description=Added Attribute in Type3)"
                )
                mappedResult shouldContainAll expectedResult
                mappedResult.size shouldBe expectedResult.size
            }
            When("계층 구조를 변경하고 MV를 업데이트하면") {
                tx.transactional {
                    em.merge(type3Version2!!.apply {
                        parent = "type_1"
                    }) // Type 계층 구조 변경
                    em.createNativeQuery("DELETE FROM attribute WHERE type='${type2Version1!!.id}' AND description='Overwritten Attribute in Type2'").executeUpdate() // 상속하던 속성 삭제
                    em.createNativeQuery("REFRESH MATERIALIZED VIEW type_attributes").executeUpdate()
                }
                Then("TypeHierarchyCloser 테이블에 변경된 계층 구조가 저장된다") {
                    val mappedResult = dumpTypeAttributes()
                    println(mappedResult)
                    val expectedResult = listOf(
                        "type_1:common_attr [1970-01-01~2999-12-31] (description=Common Attribute in Type1)",
                        "type_2:common_attr [1990-01-01~2000-01-01] (description=Common Attribute in Type1)",   // 오버라이드 속성이 제거되고 부모를 상속한다
                        "type_2:unique_attr [1990-01-01~2000-01-01] (description=Unique Attribute in Type2)",
                        "type_2:common_attr [2000-01-01~2999-12-31] (description=Overwritten Attribute in Type2)",
                        "type_3:common_attr [1990-01-01~2000-01-01] (description=Common Attribute in Type1)",   // 상속된 타입에도 부모의 오버라이드 속성이 제거되고 조부모를 상속한다
                        "type_3:exclusive_attr [1990-01-01~2000-01-01] (description=Exclusive Attribute in Type3)",
                        "type_3:unique_attr [1990-01-01~2000-01-01] (description=Unique Attribute in Type2)",
                        "type_3:common_attr [2000-01-01~2999-12-31] (description=Common Attribute in Type1)",
                        "type_3:exclusive_attr [2000-01-01~2999-12-31] (description=Exclusive Attribute in Type3)",
                        "type_3:exclusive_attr2 [2000-01-01~2999-12-31] (description=Added Attribute in Type3)" // 부모가 변경되어 type_3의 type_2 상속 속성 unique_attr가 제거된다
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