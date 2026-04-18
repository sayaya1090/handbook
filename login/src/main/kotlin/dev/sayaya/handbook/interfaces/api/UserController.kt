package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * 현재 인증 사용자 정보 조회 컨트롤러.
 *
 * **책임:** JWT에서 추출한 사용자 UUID(sub) 와 이름을 반환한다.
 * 프론트엔드에서 로그인 상태 확인 및 사용자 표시명 획득에 사용한다.
 *
 * **주의:** `id` 응답 필드는 JWT `sub`(영구 사용자 UUID) 를 우선 사용하고, 레거시 토큰
 * 호환을 위해 없을 때 `jti` 로 폴백한다. 소비자 전환(Phase 1b 이후) 후 폴백 제거 예정.
 */
@RestController
class UserController {
    @GetMapping(value = ["/user"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun user(@AuthenticationPrincipal authentication: UserAuthentication): Mono<UserResponse> {
        return Mono.just(
            UserResponse(
                id = authentication.sub ?: authentication.id,
                name = authentication.username,
            )
        )
    }

    data class UserResponse(
        val id: String?,
        val name: String,
    )
}
