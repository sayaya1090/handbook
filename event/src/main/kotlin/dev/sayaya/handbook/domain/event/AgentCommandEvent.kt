package dev.sayaya.handbook.domain.event

import java.io.Serializable
import java.util.*

/**
 * 에이전트 커맨드 이벤트.
 *
 * AI 에이전트가 실행 계획의 각 단계를 수행할 때 발행한다.
 * event-broadcaster를 통해 워크스페이스의 모든 멤버에게 브로드캐스트되어,
 * 에이전트가 제3의 협업자로서 동일한 이벤트 채널로 행동한다.
 *
 * payload는 에이전트 커맨드 JSON (navigate, highlight, mutate 등)
 */
data class AgentCommandEvent(
    override val id: UUID,
    override val workspace: UUID,
    override val eventType: Event.EventType = Event.EventType.AGENT_COMMAND,
    override val payload: AgentCommandPayload,
) : Event<AgentCommandEvent.AgentCommandPayload> {

    data class AgentCommandPayload(
        val seq: Int,
        val type: String,
        val target: Map<String, Any>? = null,
        val description: String? = null,
    ) : Serializable
}
