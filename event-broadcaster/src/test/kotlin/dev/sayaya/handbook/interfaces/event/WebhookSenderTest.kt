package dev.sayaya.handbook.interfaces.event

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

class WebhookSenderTest : DescribeSpec({
    val webClient = mockk<WebClient>()
    val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
    val requestBodySpec = mockk<WebClient.RequestBodySpec>()
    val responseSpec = mockk<WebClient.ResponseSpec>()
    val sender = WebhookSender(webClient, maxRetries = 1, retryBackoff = Duration.ofMillis(1))

    describe("WebhookSender") {
        it("send: 웹훅을 전송한다") {
            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
            every { requestBodySpec.contentType(any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
            every { requestBodySpec.retrieve() } returns responseSpec
            every { responseSpec.toBodilessEntity() } returns Mono.empty()

            sender.send("http://webhook", "payload")

            verify { webClient.post() }
        }
        it("실패 시 재시도 후 무시한다") {
            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
            every { requestBodySpec.contentType(any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
            every { requestBodySpec.retrieve() } returns responseSpec
            every { responseSpec.toBodilessEntity() } returns Mono.error(RuntimeException("Connection failed"))

            sender.send("http://webhook", "payload")
            // No exception means error is handled by onErrorResume
        }
    }
})
