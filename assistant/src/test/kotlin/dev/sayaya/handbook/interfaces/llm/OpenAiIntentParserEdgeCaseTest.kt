package dev.sayaya.handbook.interfaces.llm

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.module.kotlin.jacksonObjectMapper

class OpenAiIntentParserEdgeCaseTest : DescribeSpec({
    val webClient = mockk<WebClient>()
    val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
    val requestBodySpec = mockk<WebClient.RequestBodySpec>()
    val responseSpec = mockk<WebClient.ResponseSpec>()
    val objectMapper = jacksonObjectMapper()
    val parser = OpenAiIntentParser(webClient, objectMapper, "key")

    describe("OpenAiIntentParser Edge Cases") {
        it("parse: choices가 비어있으면 에러를 발생시킨다") {
            val responseBody = mapOf("choices" to emptyList<Map<String, Any>>())
            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/chat/completions") } returns requestBodySpec
            every { requestBodySpec.header(any(), any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
            every { requestBodySpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(Map::class.java) } returns Mono.just(responseBody)

            StepVerifier.create(parser.parse("hello", "context"))
                .expectError(NoSuchElementException::class.java)
                .verify()
        }
    }
})
