package dev.sayaya.handbook.interfaces.authentication

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 보안 예외 처리 관련 빈 설정.
 *
 * **책임:** 인증/인가 실패 시 사용되는 예외 핸들러와 진입점 빈을 등록한다.
 *
 * **의존관계:**
 * - [AuthenticationConfig] — 만료 토큰 핸들러에서 사용하는 설정
 * - [NoWwwAuthenticateEntryPoint] — WWW-Authenticate 헤더 없이 401 응답
 * - [ExpiredTokenExceptionHandler] — 만료된 JWT 처리
 * - [AuthorizationExceptionHandler] — 인가 실패 처리
 *
 * **주의:** [AuthenticationAutoConfig]에서 @Import로 포함된다.
 */
@Configuration
class SecurityExceptionConfig {
    /**
     * 인증 진입점 빈을 생성한다.
     *
     * @return WWW-Authenticate 헤더가 없는 인증 진입점
     */
    @Bean fun noWwwAuthenticateEntryPoint() = NoWwwAuthenticateEntryPoint()

    /**
     * 만료된 토큰 예외 핸들러 빈을 생성한다.
     *
     * @param config 인증 설정
     * @return 만료된 토큰 예외 핸들러
     */
    @Bean fun expiredTokenExceptionHandler(config: AuthenticationConfig) = ExpiredTokenExceptionHandler(config)

    /**
     * 인증 예외 핸들러 빈을 생성한다.
     *
     * @return 인증 예외 핸들러
     */
    @Bean fun authorizationExceptionHandler() = AuthorizationExceptionHandler()
}
