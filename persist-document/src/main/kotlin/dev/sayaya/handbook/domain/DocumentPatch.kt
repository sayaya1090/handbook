package dev.sayaya.handbook.domain

import java.util.*

/**
 * 문서 부분 업데이트 요청.
 * 변경된 필드(data)만 포함하며, 서버에서 기존 JSONB와 머지한다.
 */
data class DocumentPatch(
    val id: UUID,
    val rev: Long,
    val data: Map<String, String?>,
)
