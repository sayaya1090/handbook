package dev.sayaya.handbook.domain.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * AI 에이전트 커맨드 이벤트.
 *
 * **책임:** AI 에이전트가 실행 계획의 각 단계를 수행할 때 발행한다.
 * event-broadcaster를 통해 워크스페이스의 모든 멤버에게 브로드캐스트되어,
 * 에이전트가 제3의 협업자로서 동일한 이벤트 채널로 행동한다.
 *
 * **주의:** payload의 type 필드는 커맨드 종류(navigate, highlight, mutate 등)를 나타내며,
 * target은 커맨드별로 다른 구조를 갖는다.
 *
 * @see AgentCommandPayload 커맨드 페이로드 구조
 */
data class AgentCommandEvent(
    override val id: UUID,
    override val workspace: UUID,
    @JsonProperty("event_type") override val eventType: Event.EventType = Event.EventType.AGENT_COMMAND,
    override val payload: AgentCommandPayload,
) : Event<AgentCommandEvent.AgentCommandPayload> {

    data class AgentCommandPayload(
        val seq: Int,
        val type: String,
        val target: Map<String, Any>? = null,
        val description: String? = null,
    )
}
