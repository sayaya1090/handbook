package dev.sayaya.handbook.interfaces.database

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import reactor.core.publisher.Flux
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant
import java.util.*

class AdapterTest : DescribeSpec({
    val objectMapper = jacksonObjectMapper()

    describe("R2dbcLayoutSearchRepositoryAdapter") {
        val repo = mockk<R2dbcLayoutEntityRepository>()
        val adapter = R2dbcLayoutSearchRepositoryAdapter(repo, objectMapper)
        
        it("toDomain 매핑 검증") {
            val id = UUID.randomUUID()
            val ws = UUID.randomUUID()
            val now = Instant.now()
            val later = now.plusSeconds(3600)
            val entity = R2dbcLayoutEntity(id, ws, now, later, """{"t1":{"x":10,"y":20,"width":100,"height":50}}""")
            
            val domain = adapter.toDomain(entity)
            domain.id() shouldBe id.toString()
            domain.positions().get("t1")?.x() shouldBe 10
            domain.positions().get("t1")?.width() shouldBe 100
        }
    }

    describe("R2dbcTypeSearchRepositoryAdapter") {
        val typeRepo = mockk<R2dbcTypeEntityRepository>()
        val attrRepo = mockk<R2dbcAttributeEntityRepository>()
        val attrMapper = AttributeEntityMapper()
        val adapter = R2dbcTypeSearchRepositoryAdapter(typeRepo, attrRepo, attrMapper)

        it("hydrate: 빈 엔티티 목록 처리") {
            val result = adapter.hydrate(UUID.randomUUID(), Flux.empty()).collectList().block()
            result shouldBe emptyList()
        }
    }
})
