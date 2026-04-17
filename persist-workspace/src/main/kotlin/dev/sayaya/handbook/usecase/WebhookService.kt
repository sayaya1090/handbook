package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Webhook
import dev.sayaya.handbook.interfaces.database.R2dbcWebhookEntity
import dev.sayaya.handbook.interfaces.database.R2dbcWebhookRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 웹훅 등록/조회/삭제 비즈니스 로직 (유스케이스 계층).
 *
 * **책임:** 워크스페이스 단위로 웹훅을 등록, 목록 조회, 삭제하며,
 * 특정 워크스페이스와 이벤트 타입으로 활성 웹훅을 검색한다.
 *
 * **의존관계:**
 * - [R2dbcWebhookRepository] — 웹훅 영속화 (Spring Data R2DBC 자동 구현)
 *
 * **주의:** [findByWorkspaceAndEvent]는 활성 웹훅만 반환하며,
 * events 컬럼이 비어 있으면 모든 이벤트를 구독하는 것으로 간주한다.
 */
@Service
class WebhookService(
    private val webhookRepository: R2dbcWebhookRepository,
) {
    /**
     * 새 웹훅을 등록한다.
     *
     * @param webhook 등록할 웹훅 도메인 객체
     * @return 저장된 웹훅 (createdAt 포함)
     */
    fun register(webhook: Webhook): Mono<Webhook> {
        val entity = R2dbcWebhookEntity.fromDomain(webhook)
        return webhookRepository.save(entity).map { it.toDomain() }
    }

    /**
     * 워크스페이스의 모든 웹훅 목록을 조회한다.
     *
     * @param workspace 워크스페이스 ID
     * @return 웹훅 목록 (활성/비활성 모두 포함)
     */
    fun list(workspace: UUID): Flux<Webhook> {
        return webhookRepository.findByWorkspace(workspace).map { it.toDomain() }
    }

    /**
     * 웹훅을 삭제한다.
     *
     * @param id 삭제할 웹훅 ID
     * @return 완료 시그널
     */
    fun delete(id: UUID): Mono<Void> {
        return webhookRepository.deleteById(id)
    }

    /**
     * 워크스페이스 cascade 삭제의 일부로, 해당 워크스페이스의 모든 웹훅을 제거한다.
     *
     * @param workspace 워크스페이스 ID
     * @return 완료 시그널
     */
    fun deleteByWorkspace(workspace: UUID): Mono<Void> {
        return webhookRepository.deleteByWorkspace(workspace)
    }

    /**
     * 특정 워크스페이스에서 주어진 이벤트 타입을 구독하는 활성 웹훅을 조회한다.
     *
     * events가 비어 있는 웹훅은 모든 이벤트를 구독하는 것으로 간주한다.
     *
     * @param workspace 워크스페이스 ID
     * @param eventType 이벤트 타입 (예: "DOCUMENT_CREATED")
     * @return 매칭되는 활성 웹훅 목록
     */
    fun findByWorkspaceAndEvent(workspace: UUID, eventType: String): Flux<Webhook> {
        return webhookRepository.findByWorkspaceAndActiveTrue(workspace)
            .map { it.toDomain() }
            .filter { webhook -> webhook.events.isEmpty() || eventType in webhook.events }
    }
}
