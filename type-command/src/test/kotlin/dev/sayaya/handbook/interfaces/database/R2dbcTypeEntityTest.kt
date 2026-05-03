package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Type
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.util.*

class R2dbcTypeEntityTest : DescribeSpec({
    describe("R2dbcTypeEntity") {
        it("toDomain 매핑 검증") {
            val entity = R2dbcTypeEntity("t1", "v1", UUID.randomUUID(), Instant.now(), Instant.now(), "desc", true, "p1", 1L)
            val domain = entity.toDomain()
            domain.id() shouldBe "t1"
            domain.version() shouldBe "v1"
            domain.description() shouldBe "desc"
            domain.primitive() shouldBe true
            domain.parent() shouldBe "p1"
        }
        it("fromDomain 매핑 검증") {
            val type = Type.create("t1", "v1", java.time.Instant.now().toEpochMilli().toDouble(), java.time.Instant.now().toEpochMilli().toDouble()).description("desc").primitive(true).parent("p1")
            val ws = UUID.randomUUID()
            val entity = R2dbcTypeEntity.fromDomain(ws, type)
            entity.id shouldBe "t1"
            entity.version shouldBe "v1"
            entity.workspace shouldBe ws
        }
    }
})
