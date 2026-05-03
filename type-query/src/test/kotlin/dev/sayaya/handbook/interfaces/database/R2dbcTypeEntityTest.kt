package dev.sayaya.handbook.interfaces.database

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
    }
})
