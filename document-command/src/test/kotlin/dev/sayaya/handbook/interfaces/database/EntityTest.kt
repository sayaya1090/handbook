package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Document
import io.r2dbc.postgresql.codec.Json
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.util.*

class EntityTest : DescribeSpec({
    describe("R2dbcDocumentEntity") {
        it("toDomain 매핑 검증") {
            val id = UUID.randomUUID()
            val ws = UUID.randomUUID()
            val now = Instant.now()
            val later = now.plusSeconds(3600)
            val entity = R2dbcDocumentEntity(id, ws, "type", "serial", now, later, Json.of("{}"), "DRAFT", now, "user", 1L)
            val domain = entity.toDomain()
            domain.id shouldBe id
            domain.type shouldBe "type"
            domain.serial shouldBe "serial"
            domain.status shouldBe "DRAFT"
            domain.rev shouldBe 1L
        }
        it("fromDomain 매핑 검증") {
            val id = UUID.randomUUID()
            val now = Instant.now()
            val later = now.plusSeconds(3600)
            val document = Document(id, "type", "serial", now, later, now, "user", emptyMap(), "DRAFT", 1L)
            val ws = UUID.randomUUID()
            val entity = R2dbcDocumentEntity.fromDomain(ws, document, "{}")
            entity.id shouldBe id
            entity.workspace shouldBe ws
            entity.data.asString() shouldBe "{}"
        }
    }
})
