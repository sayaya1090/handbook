package dev.sayaya.handbook.domain.event

import java.io.Serializable

/**
 * 검증 요청 이벤트의 페이로드.
 *
 * @property typeId 검증 대상 타입 ID
 * @property typeVersion 검증 대상 타입 버전 (null이면 해당 타입의 모든 문서 재검증)
 * @property documentId 검증 대상 문서 ID (null이면 타입 단위 검증)
 */
@JvmRecord
data class ValidationPayload(
    val typeId: String,
    val typeVersion: String? = null,
    val documentId: String? = null,
) : Serializable
