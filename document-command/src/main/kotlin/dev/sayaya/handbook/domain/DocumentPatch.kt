package dev.sayaya.handbook.domain

import java.util.*

/**
 * 문서 부분 업데이트 요청 DTO.
 *
 * **책임:** 변경된 필드(data)만 포함하며, 서버에서 기존 JSONB와 `||` 연산자로 머지한다.
 *
 * **주의:** [rev]는 낙관적 잠금에 사용된다. DB의 현재 rev와 불일치하면
 * 409 Conflict가 반환되므로, 클라이언트는 최신 rev를 함께 전송해야 한다.
 *
 * @property id 수정 대상 문서 UUID
 * @property rev 클라이언트가 보유한 문서 리비전 (낙관적 잠금)
 * @property data 변경할 필드 맵 (null 값은 해당 필드 삭제를 의미)
 */
data class DocumentPatch(
    val id: UUID,
    val rev: Long,
    val data: Map<String, String?>,
)
