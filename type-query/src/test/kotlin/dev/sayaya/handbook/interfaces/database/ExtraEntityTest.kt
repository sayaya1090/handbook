package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.AttributeType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.r2dbc.postgresql.codec.Json
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant
import java.util.*

class ExtraEntityTest : DescribeSpec({
    describe("R2dbcLayoutEntity") {
        it("toDomain 매핑 검증") {
            val id = UUID.randomUUID()
            val ws = UUID.randomUUID()
            val now = Instant.now()
            val later = now.plusSeconds(3600)
            val entity = R2dbcLayoutEntity(id, ws, now, later, "{}")
            val domain = entity.toDomain(emptyMap())
            domain.id() shouldBe id.toString()
            domain.workspace() shouldBe ws.toString()
        }
    }
    describe("AttributeEntityMapper") {
        val mapper = AttributeEntityMapper()
        it("toDomain 매핑 검증") {
            val entity = R2dbcAttributeEntity(
                UUID.randomUUID(), "t1", "v1", UUID.randomUUID(), "a1", 1, "desc",
                Json.of("""{"type":"text"}"""), false, false, Json.of("[]"), Json.of("[]")
            )
            val domain = mapper.toDomain(entity)
            domain.name() shouldBe "a1"
            domain.description() shouldBe "desc"
            domain.type().type() shouldBe AttributeType.text().type()
        }
    }
})
