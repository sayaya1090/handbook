package dev.sayaya.handbook.interfaces.llm

import dev.sayaya.handbook.domain.*
import dev.sayaya.handbook.usecase.IntentParser
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper

/**
 * OpenAI Chat API를 사용하여 자연어 메시지를 ExecutionPlan으로 변환하는 IntentParser 구현체.
 *
 * <p><b>책임:</b> 시스템 프롬프트와 사용자 메시지를 조합하여 LLM에 전달하고,
 * JSON 응답을 ExecutionPlan(서브 에이전트 정의 포함)으로 파싱한다.
 * 선택적 context 파라미터로 서브 에이전트 역할 등 추가 지시를 전달할 수 있다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link WebClient} — OpenAI Chat API 호출</li>
 *   <li>{@link ObjectMapper} — JSON 파싱</li>
 * </ul></p>
 *
 * <p><b>주의:</b> LLM 응답이 JSON 형식을 따르지 않으면 파싱 에러가 발생한다.
 * temperature=0.2로 설정하여 결정론적 응답을 유도한다.</p>
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
                  "group": 0,
                  "order": 0,
                  "command": { "type": "COMMAND_TYPE", "target": "optional", "payload": {} },
                  "description": "step description"
                }
              ],
              "confidence": 0.0 to 1.0,
              "subAgents": []
            }

            The "group" field controls parallel execution. Steps with the same group number
            run in parallel. Groups are executed sequentially in ascending order.
            Assign the same group number to steps that are independent and can run concurrently.
            Use different group numbers for steps that must run in sequence.

            For complex tasks that can be decomposed into independent sub-tasks, you may define
            "subAgents" instead of (or in addition to) steps. Each sub-agent is a specialized
            worker with its own role and task:
            {
              "subAgents": [
                {
                  "name": "unique-name",
                  "role": "role description for this sub-agent",
                  "task": "specific task to accomplish",
                  "group": 0,
                  "dependsOn": ["other-agent-name"]
                }
              ]
            }
            Sub-agents with the same group run in parallel. "dependsOn" lists sub-agent names
            whose results this agent needs. Only use subAgents for genuinely complex,
            decomposable tasks. For simple tasks, use steps only.
        """.trimIndent()
    }

    override fun parse(userMessage: String, context: String?): Mono<ExecutionPlan> {
        val systemContent = if (context != null) "$SYSTEM_PROMPT\n\n$context" else SYSTEM_PROMPT
        val requestBody = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemContent),
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
            val order = stepNode["order"]?.asInt() ?: index
            ExecutionStep(
                group = stepNode["group"]?.asInt() ?: order,
                order = order,
                command = AgentCommand(type = type, target = target, payload = payload),
                description = stepNode["description"]?.asText() ?: "",
            )
        } ?: emptyList()
        val subAgents = tree["subAgents"]?.filter { !it.isNull }?.map { node ->
            SubAgentDefinition(
                name = node["name"]?.asText() ?: "unnamed",
                role = node["role"]?.asText() ?: "",
                task = node["task"]?.asText() ?: "",
                group = node["group"]?.asInt() ?: 0,
                dependsOn = node["dependsOn"]?.map { it.asText() } ?: emptyList(),
            )
        } ?: emptyList()
        return ExecutionPlan(intent = intent, steps = steps, confidence = confidence, subAgents = subAgents)
    }
}
