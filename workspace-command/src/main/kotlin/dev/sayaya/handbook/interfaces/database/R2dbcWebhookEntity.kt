package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Webhook
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

/**
 * webhooks 테이블에 매핑되는 R2DBC 엔티티.
 *
 * **책임:** 웹훅 도메인 객체([Webhook])와 DB 행 간의 양방향 변환을 담당한다.
 * [events] 필드는 쉼표로 구분된 문자열로 저장된다.
 *
 * **의존관계:**
 * - [Webhook] — 변환 대상 도메인 객체
 *
 * **주의:** [events] 컬럼은 DB에 쉼표 구분 문자열로 저장되므로,
 * 도메인 변환 시 split/joinToString으로 List<String>과 상호 변환한다.
 */
@Table("webhooks")
data class R2dbcWebhookEntity(
    @Id val id: UUID,
    val workspace: UUID,
    val url: String,
    val events: String,
    val active: Boolean = true,
    @CreatedDate @Column("created_at") var createdAt: Instant? = null,
) {
    /**
     * DB 엔티티를 도메인 객체로 변환한다.
     */
    fun toDomain(): Webhook = Webhook(
        id = id,
        workspace = workspace,
        url = url,
        events = if (events.isBlank()) emptyList() else events.split(","),
        active = active,
        createdAt = createdAt,
    )

    companion object {
        /**
         * 도메인 객체를 DB 엔티티로 변환한다.
         */
        fun fromDomain(webhook: Webhook): R2dbcWebhookEntity = R2dbcWebhookEntity(
            id = webhook.id,
            workspace = webhook.workspace,
            url = webhook.url,
            events = webhook.events.joinToString(","),
            active = webhook.active,
            createdAt = webhook.createdAt,
        )
    }
}
