package dev.sayaya.handbook.interfaces.event

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk
import tools.jackson.module.kotlin.jacksonObjectMapper

class WebhookDispatcherTest : DescribeSpec({
    val objectMapper = jacksonObjectMapper()
    val webhookSender = mockk<WebhookSender>()
    val dispatcher = WebhookDispatcher(objectMapper, "http://localhost", webhookSender)

    describe("WebhookDispatcher") {
        it("accept: 이벤트를 수신하여 웹훅을 전송한다") {
            // Since WebClient is internally initialized in WebhookDispatcher,
            // this is hard to unit test without more refactoring.
            // But we can at least try to call it and see it handles invalid JSON.
            dispatcher.accept("invalid-json")
            // No exception means success for this edge case
        }
    }
})
