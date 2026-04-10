package dev.sayaya.handbook.domain.event

import java.io.Serializable

/**
 * 검증 요청 이벤트의 페이로드.
 *
 * **책임:** 비동기 검증의 대상 범위를 식별한다.
 * typeId만 지정하면 해당 타입의 모든 문서를, documentId까지 지정하면 특정 문서만 검증한다.
 *
 * **주의:** @JvmRecord로 선언되어 Java interop 시 record 클래스로 취급된다.
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
