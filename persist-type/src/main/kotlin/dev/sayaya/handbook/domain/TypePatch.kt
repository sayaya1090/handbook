package dev.sayaya.handbook.domain

/**
 * 타입 부분 업데이트 요청.
 * 변경된 속성(attributes)만 포함하며, 서버에서 기존 속성에 upsert한다.
 */
data class TypePatch(
    val id: String,
    val version: String,
    val rev: Long,
    val description: String? = null,
    val attributes: List<Attribute> = emptyList(),
)
