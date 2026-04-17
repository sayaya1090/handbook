package dev.sayaya.handbook.interfaces.database

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

/**
 * webhooks 테이블에 대한 Spring Data R2DBC 자동 구현 인터페이스.
 *
 * **책임:** 웹훅 엔티티([R2dbcWebhookEntity])의 기본 CRUD와
 * 워크스페이스 기반 조회를 제공한다.
 *
 * **의존관계:**
 * - [R2dbcWebhookEntity] — 매핑 대상 엔티티
 * - Spring Data R2DBC — 인터페이스 자동 구현
 *
 * **주의:** findByWorkspace는 활성/비활성 웹훅을 모두 반환한다.
 * 활성 웹훅만 필요하면 [findByWorkspaceAndActiveTrue]를 사용한다.
 */
interface R2dbcWebhookRepository : ReactiveCrudRepository<R2dbcWebhookEntity, UUID> {
    fun findByWorkspace(workspace: UUID): Flux<R2dbcWebhookEntity>
    fun findByWorkspaceAndActiveTrue(workspace: UUID): Flux<R2dbcWebhookEntity>

    /**
     * 워크스페이스 삭제 cascade 의 일부로, 해당 워크스페이스에 속한 모든 웹훅 row 를 삭제한다.
     * Spring Data R2DBC 가 메서드명으로부터 `DELETE FROM webhooks WHERE workspace = ?` 을 자동 파생한다.
     */
    fun deleteByWorkspace(workspace: UUID): reactor.core.publisher.Mono<Void>
}
