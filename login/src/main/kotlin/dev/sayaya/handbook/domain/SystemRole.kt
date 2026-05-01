package dev.sayaya.handbook.domain

import dev.sayaya.handbook.domain.SystemRole.ADMIN
import dev.sayaya.handbook.domain.SystemRole.USER


/**
 * 시스템 전역 역할.
 *
 * **책임:** 시스템 수준의 권한을 정의한다. 모든 신규 사용자는 [USER] 역할로 생성된다.
 * [ADMIN]은 시스템 관리 기능에 접근할 수 있다.
 */
enum class SystemRole : Role {
    ADMIN,
    USER,
}
