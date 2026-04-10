package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeSearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

class TypeControllerTest : BehaviorSpec({
    val service = mockk<TypeSearchService>()
    val controller = TypeController(service)
    val client = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    val type = Type(
        id = "customer",
        version = "1.0",
        effectDateTime = Instant.parse("2026-01-01T00:00:00Z"),
        expireDateTime = Instant.parse("2026-12-31T23:59:59Z"),
        description = "고객 타입",
        primitive = false,
    )

    Given("기간 지정 타입 조회 API") {
        every { service.findByRange(workspace, any(), any()) } returns Flux.just(type)

        When("GET /workspace/{id}/types?effect_date_time=&expire_date_time=를 호출하면") {
            Then("200 OK가 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/types?effect_date_time=2026-01-01T00:00:00Z&expire_date_time=2026-12-31T23:59:59Z")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    Given("전체 타입 조회 API") {
        every { service.findByRange(workspace, null, null) } returns Flux.just(type)

        When("GET /workspace/{id}/types를 기간 없이 호출하면") {
            Then("200 OK가 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/types")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }
})
