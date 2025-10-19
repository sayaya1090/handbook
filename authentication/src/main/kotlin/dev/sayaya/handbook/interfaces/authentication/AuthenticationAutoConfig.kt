package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.domain.Pem
import dev.sayaya.handbook.domain.TokenConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter

/**
 * 인증 및 JWT 관련 자동 구성 클래스
 *
 * Spring Boot의 자동 구성 메커니즘을 통해 JWT 인증에 필요한 빈들을 등록합니다.
 * 애플리케이션에서 해당 빈을 직접 정의한 경우 자동 구성이 적용되지 않습니다.
 */
@Configuration
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(AuthenticationConfig::class, TokenConfig::class)
class AuthenticationAutoConfig {
    /**
     * PEM 키 파서 빈을 생성합니다.
     *
     * @param config JWT 토큰 설정
     * @return PEM 형식 키 파서
     */
    @Bean fun pem(config: TokenConfig) = Pem(config)

    /**
     * Claims 인증 컨버터 빈을 생성합니다.
     *
     * @return 기본 사용자 인증 컨버터
     */
    @Bean fun claimsAuthenticationConverter(): ClaimsAuthenticationConverter = UserAuthenticationConverter()

    /**
     * JWT 인증 컨버터 빈을 생성합니다.
     *
     * @param config 인증 설정
     * @return JWT 인증 컨버터
     */
    @Bean fun jwtAuthenticationConverter(config: AuthenticationConfig) = JwtAuthenticationConverter(config)

    /**
     * JWT 인증 매니저 빈을 생성합니다.
     *
     * @param pem PEM 키 파서
     * @param converter Claims 인증 컨버터
     * @return JWT 인증 매니저
     */
    @Bean fun jwtAuthenticationManager(pem: Pem, converter: ClaimsAuthenticationConverter) = JwtAuthenticationManager(pem, converter)

    /**
     * 인증 진입점 빈을 생성합니다.
     *
     * @return WWW-Authenticate 헤더가 없는 인증 진입점
     */
    @Bean fun noWwwAuthenticateEntryPoint() = NoWwwAuthenticateEntryPoint()

    /**
     * 만료된 토큰 예외 핸들러 빈을 생성합니다.
     *
     * @param config 인증 설정
     * @return 만료된 토큰 예외 핸들러
     */
    @Bean fun expiredTokenExceptionHandler(config: AuthenticationConfig) = ExpiredTokenExceptionHandler(config)

    /**
     * 인증 예외 핸들러 빈을 생성합니다.
     *
     * @return 인증 예외 핸들러
     */
    @Bean fun authorizationExceptionHandler() = AuthorizationExceptionHandler()

    /**
     * 어플리케이션의 전반적인 보안 설정을 담당하는 빈.
     *
     * JWT 기반의 인증/인가를 위한 반응형 보안 필터 체인을 구성합니다.
     * 다른 모듈에서 [SecurityWebFilterChain] 빈이 이미 정의되어 있다면, 이 설정은 적용되지 않습니다.
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

            // Clickjacking 방지를 위해 X-Frame-Options 헤더 설정
            headers { frameOptions { mode = XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN } }

            // 직접 구현한 JWT 인증 필터를 등록
            addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            // 인증 예외 발생 시 처리 방식 정의
            exceptionHandling {
                authenticationEntryPoint = noWwwAuthenticateEntryPoint
            }

            // 경로별 접근 권한 설정
            authorizeExchange {
                authorize("/actuator/**", permitAll)  // Actuator 엔드포인트는 모두 허용
                authorize(anyExchange, authenticated)   // 그 외 모든 요청은 인증 필요
            }
        }
    }
}