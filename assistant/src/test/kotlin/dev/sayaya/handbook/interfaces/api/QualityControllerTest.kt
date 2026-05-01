package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.usecase.QualityMonitorService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

class QualityControllerTest : BehaviorSpec({
    val service = mockk<QualityMonitorService>()
    val controller = QualityController(service)
    val client = WebTestClient.bindToController(controller).build()

    Given("품질 검사 API") {
        val workspace = UUID.randomUUID()
        every { service.execute(any()) } returns Mono.empty()

        When("POST /assistant/quality/scan을 호출하면") {
            Then("202 Accepted가 반환된다") {
                client.post()
                    .uri("/assistant/quality/scan?workspace=$workspace")
                    .exchange()
                    .expectStatus().isAccepted
            }
        }
    }
})
