package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.DiffResult
import dev.sayaya.handbook.usecase.TypeSearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

class TypeControllerDiffTest : BehaviorSpec({
    val service = mockk<TypeSearchService>()
    val controller = TypeController(service)
    val client = WebTestClient.bindToController(controller).build()
    val workspace = UUID.randomUUID()

    Given("타입 diff API") {
        val diffResult = DiffResult(
            changes = listOf("description: 고객 타입 -> 변경된 설명"),
            added = listOf("email"),
            removed = listOf("fax"),
        )
        every { service.diff(workspace, "customer", "1.0", "2.0") } returns Mono.just(diffResult)

        When("GET /workspaces/{id}/types/{typeId}/diff?v1=1.0&v2=2.0을 호출하면") {
            Then("200 OK + diff 결과가 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/types/customer/diff?v1=1.0&v2=2.0")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.changes[0]").isEqualTo("description: 고객 타입 -> 변경된 설명")
                    .jsonPath("$.added[0]").isEqualTo("email")
                    .jsonPath("$.removed[0]").isEqualTo("fax")
            }
        }
    }

    Given("빈 결과 diff API") {
        val emptyDiff = DiffResult()
        every { service.diff(workspace, "customer", "1.0", "1.0") } returns Mono.just(emptyDiff)

        When("동일 버전으로 diff를 호출하면") {
            Then("빈 diff 결과가 반환된다") {
                client.get()
                    .uri("/workspaces/$workspace/types/customer/diff?v1=1.0&v2=1.0")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.changes").isEmpty
                    .jsonPath("$.added").isEmpty
                    .jsonPath("$.removed").isEmpty
            }
        }
    }
})
