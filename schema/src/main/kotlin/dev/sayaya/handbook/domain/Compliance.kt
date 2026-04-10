package dev.sayaya.handbook.domain

import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 * 문서와 타입 버전 간의 호환성 검증 결과를 저장하는 값 객체(Value Object).
 *
 * 문서는 특정 타입 버전에 고정되지 않으므로, 검증 시점에 현재 유효한
 * 타입 버전들과의 호환 여부를 판별하고 그 결과를 이 객체에 기록한다.
 *
 * @property documentId 검증 대상 문서의 ID
 * @property typeId 검증 대상 타입의 ID
 * @property typeVersion 검증 대상 타입의 버전
 * @property compatible 호환 여부 (true: 호환, false: 불일치)
 * @property violations 불일치 시 위반 사유 목록 (속성명 → 위반 사유)
 * @property verifiedAt 검증 시각
 */
@JvmRecord
data class Compliance(
    val documentId: UUID,
    val typeId: String,
    val typeVersion: String,
    val compatible: Boolean,
    val violations: Map<String, String>,
    val verifiedAt: Instant,
) : Serializable {
    init {
        if (compatible) {
            require(violations.isEmpty()) { "Compatible compliance must have no violations." }
        } else {
            require(violations.isNotEmpty()) { "Incompatible compliance must have at least one violation." }
        }
    }
}
