package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.AttributeType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.r2dbc.postgresql.codec.Json
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.util.*

class AttributeEntityMapperTest : BehaviorSpec({
    val objectMapper = JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
        .changeDefaultVisibility { it.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) }
        .addModule(KotlinModule.Builder().withReflectionCacheSize(512).build())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    val mapper = AttributeEntityMapper(objectMapper)
    val workspace = UUID.randomUUID()

    Given("Attribute 도메인 객체가 주어졌을 때") {
        val attr = Attribute.create(UUID.randomUUID().toString(), "email", 1, AttributeType.text())
            .description("이메일")
            .nullable(true)
            .inherited(true)
            .readRoles(arrayOf("ADMIN"))
            .writeRoles(arrayOf("MEMBER"))

        When("toEntity를 호출하면") {
            val entity = mapper.toEntity("customer", "1.0", workspace, attr)

            Then("모든 필드가 올바르게 매핑된다") {
                entity.typeId shouldBe "customer"
                entity.typeVersion shouldBe "1.0"
                entity.workspace shouldBe workspace
                entity.name shouldBe "email"
                entity.order shouldBe 1.toShort()
                entity.description shouldBe "이메일"
                entity.nullable shouldBe true
                entity.inherited shouldBe true
                entity.attributeType.asString() shouldContain "\"type\":\"text\""
                entity.readRoles.asString() shouldContain "ADMIN"
                entity.writeRoles.asString() shouldContain "MEMBER"
            }
        }
    }

    Given("Attribute 엔티티가 주어졌을 때") {
        val entity = R2dbcAttributeEntity(
            id = UUID.randomUUID(),
            typeId = "order",
            typeVersion = "1.1",
            workspace = workspace,
            name = "amount",
            order = 2,
            description = "금액",
            attributeType = Json.of("{\"type\":\"number\",\"min\":0.0}"),
            nullable = false,
            inherited = false,
            readRoles = Json.of("[]"),
            writeRoles = Json.of("[]")
        )

        When("toDomain을 호출하면") {
            val domain = mapper.toDomain(entity)

            Then("모든 필드가 도메인 객체로 복원된다") {
                domain.name() shouldBe "amount"
                domain.order() shouldBe 2
                domain.description() shouldBe "금액"
                domain.type().type() shouldBe "number"
                domain.nullable() shouldBe false
                domain.inherited() shouldBe false
                domain.readRoles().size shouldBe 0
                domain.writeRoles().size shouldBe 0
            }
        }
    }
})
