package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.CommandType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.module.kotlin.jacksonObjectMapper

class OpenAiIntentParserTest : DescribeSpec({
    val webClient = mockk<WebClient>()
    val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
    val requestBodySpec = mockk<WebClient.RequestBodySpec>()
    val responseSpec = mockk<WebClient.ResponseSpec>()
    val objectMapper = jacksonObjectMapper()
    val parser = OpenAiIntentParser(webClient, objectMapper, "key")

    describe("OpenAiIntentParser") {
        it("parse: 자연어 메시지를 ExecutionPlan으로 파싱한다") {
            val responseBody = mapOf(
                "choices" to listOf(
                    mapOf(
                        "message" to mapOf(
                            "content" to """
                                {
                                  "intent": "test intent",
                                  "steps": [
                                    { "order": 0, "command": { "type": "NAVIGATE", "target": "/test" }, "description": "go to test" }
                                  ],
                                  "confidence": 0.8
                                }
                            """.trimIndent()
                        )
                    )
                )
            )

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/chat/completions") } returns requestBodySpec
            every { requestBodySpec.header(any(), any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
            every { requestBodySpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(Map::class.java) } returns Mono.just(responseBody)

            StepVerifier.create(parser.parse("hello", null))
                .assertNext { plan ->
                    plan.intent shouldBe "test intent"
                    plan.steps.size shouldBe 1
                    plan.steps[0].command.type shouldBe CommandType.NAVIGATE
                    plan.confidence shouldBe 0.8
                }
                .verifyComplete()
        }
    }
})
