package dev.sayaya.handbook.interfaces.authentication

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter

/**
 * 인증 및 JWT 관련 자동 구성 클래스.
 *
 * **책임:** SecurityWebFilterChain 기본 빈을 등록한다. JWT 인증 빈은 [JwtAuthenticationConfig]에,
 * 예외 처리 빈은 [SecurityExceptionConfig]에 위임한다.
 *
 * **의존관계:**
 * - [JwtAuthenticationConfig] — PEM, JWT 컨버터/매니저, Claims 컨버터
 * - [SecurityExceptionConfig] — 인증 진입점, 예외 핸들러
 *
 * **주의:** 다른 모듈에서 [SecurityWebFilterChain] 빈이 이미 정의되어 있으면
 * [securityFilterChain]은 적용되지 않는다 (ConditionalOnMissingBean).
 */
@Configuration
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(AuthenticationConfig::class)
@Import(JwtAuthenticationConfig::class, SecurityExceptionConfig::class)
class AuthenticationAutoConfig {
    /**
     * 어플리케이션의 전반적인 보안 설정을 담당하는 빈.
     *
     * JWT 기반의 인증/인가를 위한 반응형 보안 필터 체인을 구성한다.
     * 다른 모듈에서 [SecurityWebFilterChain] 빈이 이미 정의되어 있다면, 이 설정은 적용되지 않는다.
     *
     * @param jwtAuthenticationConverter HTTP 요청에서 JWT를 추출하여 초기 인증 토큰으로 변환하는 컨버터
     * @param jwtAuthenticationManager JWT의 유효성을 검증하고 최종 인증 객체를 생성하는 매니저
     * @param noWwwAuthenticateEntryPoint 인증 실패 시 `WWW-Authenticate` 헤더 없이 401 응답을 보내는 진입점
     */
    @Bean
    @ConditionalOnMissingBean(SecurityWebFilterChain::class)
    fun securityFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter,
        jwtAuthenticationManager: JwtAuthenticationManager,
        noWwwAuthenticateEntryPoint: NoWwwAuthenticateEntryPoint
    ): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter)

        return http {
            // STATELESS API를 위해 CSRF, HTTP Basic, 폼 로그인 비활성화
            csrf { disable() }
            httpBasic { disable() }
            formLogin { disable() }

            // 보안 응답 헤더 설정
            headers {
                frameOptions { mode = XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN }
                contentTypeOptions { }
                hsts { }
                contentSecurityPolicy { policyDirectives = "default-src 'self'; script-src 'self' 'unsafe-eval'; style-src 'self' 'unsafe-inline'" }
            }

            // 직접 구현한 JWT 인증 필터를 등록
            addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            // 인증 예외 발생 시 처리 방식 정의
            exceptionHandling {
                authenticationEntryPoint = noWwwAuthenticateEntryPoint
            }

            // 경로별 접근 권한 설정
            authorizeExchange {
                authorize("/actuator/health/**", permitAll)
                authorize("/actuator/info", permitAll)
                authorize("/actuator/**", authenticated)
                authorize(anyExchange, authenticated)
            }
        }
    }
}
