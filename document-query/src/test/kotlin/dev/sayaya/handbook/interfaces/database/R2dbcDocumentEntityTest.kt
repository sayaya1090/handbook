package dev.sayaya.handbook.interfaces.database

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.util.*

class R2dbcDocumentEntityTest : DescribeSpec({
    describe("R2dbcDocumentEntity") {
        it("필드 초기화 검증") {
            val id = UUID.randomUUID()
            val ws = UUID.randomUUID()
            val now = Instant.now()
            val entity = R2dbcDocumentEntity(ws, id, "type", "serial", now, now, now, "user", "{}")
            entity.id shouldBe id
            entity.workspace shouldBe ws
            entity.data shouldBe "{}"
        }
    }
})
