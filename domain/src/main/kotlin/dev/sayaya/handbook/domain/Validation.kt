package dev.sayaya.handbook.domain

import java.io.Serializable

@JvmRecord
data class Validation (
    val status: Status,
    val result: Map<String, Boolean>?
): Serializable {
    companion object {
        enum class Status {
            NEW,        // 작업이 생성되어 처리 대기 중
            PROCESSING, // 작업이 워커에 의해 처리 중
            DONE,       // 작업 성공적으로 완료
            FAILED      // 작업 실패
        }
    }
}