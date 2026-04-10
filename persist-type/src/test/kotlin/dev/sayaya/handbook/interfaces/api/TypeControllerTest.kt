package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class TypeControllerTest : BehaviorSpec({
    val service = mockk<TypeService>()
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

    // UC-PT1: 타입 조회 API
    Given("타입 조회 API") {
        every { service.findByPeriod(workspace, any(), any()) } returns Flux.just(type)

        When("GET /workspace/{id}/types를 호출하면") {
            Then("200 OK가 반환된다") {
                client.get()
                    .uri("/workspace/$workspace/types?effect_date_time=2026-01-01T00:00:00Z&expire_date_time=2026-12-31T23:59:59Z")
                    .header("Accept", "application/vnd.sayaya.handbook.v1+json")
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    // UC-PT2: 타입 저장 API
    Given("타입 저장 API") {
        every { service.save(workspace, any()) } returns Flux.just(type)

        When("PUT /workspace/{id}/types를 호출하면") {
            Then("200 OK가 반환된다") {
                client.put()
                    .uri("/workspace/$workspace/types")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(listOf(type))
                    .exchange()
                    .expectStatus().isOk
            }
        }
    }

    // UC-PT3: 타입 삭제 API
    Given("타입 삭제 API") {
        every { service.delete(workspace, any()) } returns Mono.empty()

        When("DELETE /workspace/{id}/types를 호출하면") {
            Then("204 No Content가 반환된다") {
                client.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/workspace/$workspace/types")
                    .header("Content-Type", "application/vnd.sayaya.handbook.v1+json")
                    .bodyValue(listOf(type))
                    .exchange()
                    .expectStatus().isNoContent
            }
        }
    }
})
