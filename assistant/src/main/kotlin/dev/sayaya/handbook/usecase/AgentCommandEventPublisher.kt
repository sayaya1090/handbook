package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AgentCommand
import java.util.UUID

/**
 * 에이전트 커맨드를 워크스페이스 이벤트로 발행하는 포트.
 * Kafka를 통해 event-broadcaster로 전달되어 워크스페이스 전체에 브로드캐스트된다.
 */
interface AgentCommandEventPublisher {
    fun publish(workspace: UUID, seq: Int, command: AgentCommand)
}
