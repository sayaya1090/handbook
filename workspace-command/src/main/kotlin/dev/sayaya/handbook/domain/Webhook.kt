package dev.sayaya.handbook.domain

import java.time.Instant
import java.util.*

/**
 * 웹훅 등록 정보를 나타내는 도메인 객체.
 *
 * **책임:** 특정 워크스페이스에서 발생하는 이벤트를 외부 URL로 전달하기 위한
 * 웹훅 구독 정보를 캡슐화한다.
 *
 * **의존관계:** 없음 (순수 도메인 객체)
 *
 * **주의:** [events]는 구독할 이벤트 타입 목록이다 (예: "DOCUMENT_CREATED", "TYPE_DELETED").
 * 빈 목록이면 모든 이벤트를 수신한다.
 *
 * @property id 웹훅 고유 식별자
 * @property workspace 웹훅이 등록된 워크스페이스 ID
 * @property url 콜백을 전달할 대상 URL
 * @property events 구독할 이벤트 타입 목록 (빈 목록 = 전체 이벤트)
 * @property active 웹훅 활성화 여부
 * @property createdAt 등록 시각
 */
data class Webhook(
    val id: UUID,
    val workspace: UUID,
    val url: String,
    val events: List<String>,
    val active: Boolean = true,
    val createdAt: Instant? = null,
)
