package dev.sayaya.handbook.domain

/**
 * 사용자 계정 상태.
 *
 * **책임:** 계정 활성화/비활성화 상태를 나타낸다.
 * 비활성화된 사용자는 인증이 거부된다.
 */
enum class State {
    ACTIVATED,
    INACTIVATED,
}
