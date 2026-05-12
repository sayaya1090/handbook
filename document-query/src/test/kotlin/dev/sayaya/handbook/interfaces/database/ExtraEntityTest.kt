package dev.sayaya.handbook.interfaces.database

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant
import java.util.*

class ExtraEntityTest : DescribeSpec({
    describe("R2dbcDocumentSearchRepository Mapping") {
        val objectMapper = jacksonObjectMapper()
        val repo = R2dbcDocumentSearchRepository(mockk(relaxed = true), objectMapper)
        
        it("toDomain mapping logic") {
            val now = Instant.now()
            val later = now.plusSeconds(3600)
            val entity = R2dbcDocumentEntity(
                UUID.randomUUID(), UUID.randomUUID(), "type", "serial", 
                now, later, now, "user", """{"key":"value"}""", 0L
            )
            val domain = repo.toDomain(entity)
            domain.serial() shouldBe "serial"
        }
    }
})
