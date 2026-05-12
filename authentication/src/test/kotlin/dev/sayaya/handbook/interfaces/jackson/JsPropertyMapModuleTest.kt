package dev.sayaya.handbook.interfaces.jackson

import dev.sayaya.handbook.domain.TypeLayout
import dev.sayaya.handbook.domain.Position
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue

class JsPropertyMapModuleTest : DescribeSpec({
    val objectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .addModule(JsPropertyMapModule())
        .build()

    describe("JsPropertyMapModule") {
        it("TypeLayout 내의 JsPropertyMap<Position> 필드를 JVM에서 역직렬화할 수 있다") {
            // Given
            val json = """
                {
                    "id": "layout-1",
                    "workspace": "00000000-0000-0000-0000-000000000000",
                    "effect_date_time": 1704067200000,
                    "expire_date_time": 4102444800000,
                    "positions": {
                        "type-A": { "x": 100, "y": 200 },
                        "type-B": { "x": 300, "y": 400 }
                    }
                }
            """.trimIndent()

            // When
            val layout = objectMapper.readValue<TypeLayout>(json)

            // Then
            layout shouldNotBe null
            layout.id() shouldBe "layout-1"
            
            val positions = layout.positions()
            positions shouldNotBe null
            
            val posA = positions.get("type-A")
            posA shouldNotBe null
            posA.x() shouldBe 100.0
            posA.y() shouldBe 200.0
            
            val posB = positions.get("type-B")
            posB shouldNotBe null
            posB.x() shouldBe 300.0
            posB.y() shouldBe 400.0
        }

        it("JsPropertyMap Proxy는 Map 인터페이스로도 접근 가능하며 Jackson에 의해 다시 직렬화될 수 있다") {
            // Given
            val json = """{"key1": "value1", "key2": "value2"}"""
            val map = objectMapper.readValue<jsinterop.base.JsPropertyMap<String>>(json)

            // When
            val mapInterface = map as Map<String, String>
            
            // Then
            mapInterface["key1"] shouldBe "value1"
            mapInterface["key2"] shouldBe "value2"
            mapInterface.size shouldBe 2
            
            // Re-serialization check
            val reSerialized = objectMapper.writeValueAsString(map)
            val expectedJson = """{"key1":"value1","key2":"value2"}"""
            reSerialized shouldBe expectedJson
        }
    }
})
