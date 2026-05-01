package dev.sayaya.handbook.interfaces.llm

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.module.kotlin.jacksonObjectMapper

class OpenAiIntentParserPayloadTest : DescribeSpec({
    val webClient = mockk<WebClient>()
    val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
    val requestBodySpec = mockk<WebClient.RequestBodySpec>()
    val responseSpec = mockk<WebClient.ResponseSpec>()
    val objectMapper = jacksonObjectMapper()
    val parser = OpenAiIntentParser(webClient, objectMapper, "key")

    describe("OpenAiIntentParser Payload Parsing") {
        it("parse: payload가 있는 단계를 파싱한다") {
            val responseBody = mapOf(
                "choices" to listOf(
                    mapOf(
                        "message" to mapOf(
                            "content" to """
                                {
                                  "intent": "payload test",
                                  "steps": [
                                    { 
                                      "order": 0, 
                                      "command": { "type": "NOTIFY", "payload": { "msg": "hi" } }, 
                                      "description": "desc" 
                                    }
                                  ],
                                  "confidence": 1.0
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
                    plan.steps[0].command.payload shouldBe mapOf("msg" to "hi")
                }
                .verifyComplete()
        }
        it("parse: subAgents를 파싱한다") {
            val responseBody = mapOf(
                "choices" to listOf(
                    mapOf(
                        "message" to mapOf(
                            "content" to """
                                {
                                  "intent": "subagent test",
                                  "steps": [],
                                  "confidence": 1.0,
                                  "subAgents": [
                                    { "name": "agent-1", "role": "role-1", "task": "task-1", "group": 1, "dependsOn": ["other"] }
                                  ]
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
                    plan.subAgents.size shouldBe 1
                    plan.subAgents[0].name shouldBe "agent-1"
                    plan.subAgents[0].dependsOn shouldBe listOf("other")
                }
                .verifyComplete()
        }
    }
})
