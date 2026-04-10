package dev.sayaya.handbook.domain

/**
 * 타입 부분 업데이트 요청 DTO.
 *
 * **책임:** 변경된 속성(attributes)만 포함하며, 서버에서 기존 속성에 이름 기준 upsert(삭제 후 재삽입)한다.
 *
 * **주의:** [rev]는 낙관적 잠금에 사용된다. DB의 현재 rev와 불일치하면
 * 409 Conflict가 반환되므로, 클라이언트는 최신 rev를 함께 전송해야 한다.
 *
 * @property id 수정 대상 타입 ID (이름 문자열)
 * @property version 수정 대상 타입 버전
 * @property rev 클라이언트가 보유한 타입 리비전 (낙관적 잠금)
 * @property description 변경할 설명 (null이면 변경하지 않음)
 * @property attributes 변경할 속성 목록 (빈 리스트면 속성 변경 없음)
 */
data class TypePatch(
    val id: String,
    val version: String,
    val rev: Long,
    val description: String? = null,
    val attributes: List<Attribute> = emptyList(),
)
