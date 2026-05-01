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
            val entity = R2dbcDocumentEntity(
                UUID.randomUUID(), UUID.randomUUID(), "type", "serial", 
                Instant.now(), Instant.now(), Instant.now(), "user", """{"key":"value"}"""
            )
            val domain = repo.toDomain(entity)
            domain.serial shouldBe "serial"
            domain.data shouldBe mapOf("key" to "value")
        }
    }
})
