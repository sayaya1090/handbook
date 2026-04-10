package dev.sayaya.handbook.interfaces.llm

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.AgentCommand
import dev.sayaya.handbook.domain.CommandType
import dev.sayaya.handbook.domain.ExecutionPlan
import dev.sayaya.handbook.domain.ExecutionStep
import dev.sayaya.handbook.usecase.IntentParser
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * OpenAI Chat API를 사용하여 자연어 메시지를 ExecutionPlan으로 변환하는 IntentParser 구현체.
 */
class OpenAiIntentParser(
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val apiKey: String,
    private val model: String = "gpt-4o",
) : IntentParser {

    companion object {
        private val SYSTEM_PROMPT = """
            You are a UI automation assistant for Handbook, a document management system.
            Analyze the user's message and produce an execution plan as JSON.

            Available command types:
            - NAVIGATE: Navigate to a page. target = page identifier.
            - HIGHLIGHT: Highlight an element. target = CSS selector.
            - ATTENTION: Draw attention to an element. target = CSS selector.
            - SCROLL: Scroll to an element. target = CSS selector.
            - PREVIEW: Show a preview. payload contains preview data.
            - MUTATE: Modify data. payload contains mutation details.
            - NOTIFY: Show a notification. payload contains level and message.
            - PROGRESS: Show progress. payload contains percentage.
            - AWAIT_CONFIRM: Wait for user confirmation. payload contains prompt.
            - COMPLETE: Mark operation as complete.

            Respond ONLY with JSON in this format (no markdown, no explanation):
            {
              "intent": "short summary of user intent",
              "steps": [
                {
                  "order": 0,
                  "command": { "type": "COMMAND_TYPE", "target": "optional", "payload": {} },
                  "description": "step description"
                }
              ],
              "confidence": 0.0 to 1.0
            }
        """.trimIndent()
    }

    override fun parse(userMessage: String): Mono<ExecutionPlan> {
        val requestBody = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to SYSTEM_PROMPT),
                mapOf("role" to "user", "content" to userMessage),
            ),
            "temperature" to 0.2,
        )

        return webClient.post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { response -> extractContent(response) }
            .map { json -> parseExecutionPlan(json) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractContent(response: Map<*, *>): String {
        val choices = response["choices"] as? List<Map<String, Any>> ?: error("No choices in response")
        val message = choices.first()["message"] as? Map<String, Any> ?: error("No message in choice")
        return message["content"] as? String ?: error("No content in message")
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseExecutionPlan(json: String): ExecutionPlan {
        val tree = objectMapper.readTree(json)
        val intent = tree["intent"]?.asText() ?: "unknown"
        val confidence = tree["confidence"]?.asDouble() ?: 0.0
        val steps = tree["steps"]?.mapIndexed { index, stepNode ->
            val commandNode = stepNode["command"]
            val type = CommandType.valueOf(commandNode["type"].asText())
            val target = commandNode["target"]?.asText()
            val payload = commandNode["payload"]?.let {
                if (it.isNull || it.isEmpty) null
                else objectMapper.convertValue(it, Map::class.java) as Map<String, Any>
            }
            ExecutionStep(
                order = stepNode["order"]?.asInt() ?: index,
                command = AgentCommand(type = type, target = target, payload = payload),
                description = stepNode["description"]?.asText() ?: "",
            )
        } ?: emptyList()
        return ExecutionPlan(intent = intent, steps = steps, confidence = confidence)
    }
}
