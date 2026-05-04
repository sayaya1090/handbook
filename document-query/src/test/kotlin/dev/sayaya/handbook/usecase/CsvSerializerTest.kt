package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import java.time.Instant
import java.util.*

/**
 * UC-SD5: 문서 내보내기 (CSV 직렬화)
 */
class CsvSerializerTest : BehaviorSpec({
    fun createDataMap(data: Map<String, String>): jsinterop.base.JsPropertyMap<String> {
        return java.lang.reflect.Proxy.newProxyInstance(
            jsinterop.base.JsPropertyMap::class.java.classLoader,
            arrayOf(jsinterop.base.JsPropertyMap::class.java, Map::class.java)
        ) { _, method, args ->
            if (method.declaringClass == Map::class.java) {
                method.invoke(data, *(args ?: emptyArray()))
            } else when (method.name) {
                "get" -> data[args[0] as String]
                "set" -> null
                "forEach" -> null
                else -> null
            }
        } as jsinterop.base.JsPropertyMap<String>
    }

    Given("문서 리스트가 주어졌을 때") {
        val doc1 = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-001",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            createDataMap(mapOf("name" to "홍길동", "email" to "hong@test.com"))
        )
        val doc2 = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-002",
            Instant.parse("2026-02-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-2",
            createDataMap(mapOf("name" to "김영희", "phone" to "010-1234-5678"))
        )

        When("CSV로 직렬화하면") {
            val csv = CsvSerializer.serialize(listOf(doc1, doc2))
            val lines = csv.trim().lines()

            Then("헤더 행이 고정 컬럼 + 동적 컬럼을 포함한다") {
                lines[0] shouldContain "type"
                lines[0] shouldContain "serial"
                lines[0] shouldContain "effect_date_time"
                lines[0] shouldContain "expire_date_time"
                lines[0] shouldContain "status"
                lines[0] shouldContain "email"
                lines[0] shouldContain "name"
                lines[0] shouldContain "phone"
            }
            Then("데이터 행이 2개 생성된다") {
                lines.size shouldBe 3 // header + 2 data rows
            }
            Then("첫 번째 데이터 행에 CUST-001이 포함된다") {
                lines[1] shouldContain "CUST-001"
                lines[1] shouldContain "홍길동"
            }
            Then("두 번째 데이터 행에 CUST-002가 포함된다") {
                lines[2] shouldContain "CUST-002"
                lines[2] shouldContain "김영희"
            }
        }
    }

    Given("빈 문서 리스트가 주어졌을 때") {
        When("CSV로 직렬화하면") {
            val csv = CsvSerializer.serialize(emptyList())
            val lines = csv.trim().lines()

            Then("헤더 행만 반환된다") {
                lines.size shouldBe 1
                lines[0] shouldStartWith "type"
            }
        }
    }

    Given("쉼표가 포함된 데이터가 주어졌을 때") {
        val doc = Document.create(
            UUID.randomUUID().toString(),
            "customer",
            "CUST-003",
            Instant.parse("2026-01-01T00:00:00Z").toEpochMilli().toDouble(),
            Instant.parse("2026-12-31T23:59:59Z").toEpochMilli().toDouble(),
            Instant.now().toEpochMilli().toDouble(),
            "user-1",
            createDataMap(mapOf("address" to "서울시 강남구, 역삼동"))
        )

        When("CSV로 직렬화하면") {
            val csv = CsvSerializer.serialize(listOf(doc))

            Then("쉼표를 포함한 값이 따옴표로 이스케이프된다") {
                csv shouldContain "\"서울시 강남구, 역삼동\""
            }
        }
    }
})
