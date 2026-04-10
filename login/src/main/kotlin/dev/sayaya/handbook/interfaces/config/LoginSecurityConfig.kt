package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.authentication.JwtAuthenticationConverter
import dev.sayaya.handbook.interfaces.authentication.JwtAuthenticationManager
import dev.sayaya.handbook.interfaces.authentication.NoWwwAuthenticateEntryPoint
import dev.sayaya.handbook.usecase.TokenPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

/**
 * Spring Security 웹 필터 체인 및 OAuth2/JWT 인증 설정.
 *
 * **책임:** SecurityWebFilterChain을 구성하여 OAuth2 로그인, JWT 인증 필터, 로그아웃,
 * 경로별 접근 제어를 설정한다. 쿠키 생성/삭제 로직은 [AuthenticationCookieService]에 위임한다.
 *
 * **의존관계:**
 * - [AuthenticationCookieService] — 인증 쿠키 설정/삭제
 * - [AuthenticationUrlConfig] — 인증 후 리다이렉트 URL
 * - [TokenPublisher] — OAuth2 사용자 → JWT 발급
 *
 * **주의:** CSRF는 비활성화되어 있으며, `/oauth2/`, `/auth/`, `/menus`는 인증 없이 접근 가능하다.
 */
@Configuration
class LoginSecurityConfig(
    private val cookieService: AuthenticationCookieService,
    private val urlConfig: AuthenticationUrlConfig,
    private val tokenPublisher: TokenPublisher,
) {
    @Bean
    fun securityFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter,
        jwtAuthenticationManager: JwtAuthenticationManager,
        noWwwAuthenticateEntryPoint: NoWwwAuthenticateEntryPoint,
    ): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter)

        return http {
            csrf { disable() }
            httpBasic { disable() }
            formLogin { disable() }
            headers {
                frameOptions { mode = XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN }
                contentTypeOptions { }
                hsts { }
            }
            addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            exceptionHandling { authenticationEntryPoint = noWwwAuthenticateEntryPoint }
            oauth2Login {
                authenticationSuccessHandler = oauthSuccessHandler()
            }
            logout {
                logoutUrl = "/oauth2/logout"
                logoutSuccessHandler = logoutSuccessHandler()
            }
            authorizeExchange {
                authorize("/oauth2/**", permitAll)
                authorize("/auth/**", permitAll)
                authorize("/menus", permitAll)
                authorize("/actuator/health/**", permitAll)
                authorize("/actuator/info", permitAll)
                authorize("/actuator/**", authenticated)
                authorize(anyExchange, authenticated)
            }
        }
    }

    private fun oauthSuccessHandler(): ServerAuthenticationSuccessHandler {
        return ServerAuthenticationSuccessHandler { webFilterExchange, authentication ->
            val oauth2Token = authentication as OAuth2AuthenticationToken
            val provider = oauth2Token.authorizedClientRegistrationId
            val principal = oauth2Token.principal ?: return@ServerAuthenticationSuccessHandler Mono.error(IllegalStateException("OAuth2 principal is null"))
            tokenPublisher.publish(provider, principal).flatMap { token ->
                cookieService.sendAuthenticationCookie(webFilterExchange.exchange, token)
                    .then(redirect(webFilterExchange.exchange, urlConfig.loginRedirectUri))
            }
        }
    }

    private fun logoutSuccessHandler(): ServerLogoutSuccessHandler {
        return ServerLogoutSuccessHandler { webFilterExchange, _ ->
            cookieService.clearAuthenticationCookie(webFilterExchange.exchange)
                .then(redirect(webFilterExchange.exchange, urlConfig.logoutRedirectUri))
        }
    }

    /**
     * 리다이렉트 URI를 검증하고 안전한 경우에만 리다이렉트를 수행한다.
     *
     * 상대 경로("/"로 시작)만 허용하며, 절대 URL(외부 도메인)은 "/"로 대체한다.
     */
    private fun redirect(exchange: ServerWebExchange, uri: String): Mono<Void> {
        val safeUri = if (uri.startsWith("/") && !uri.startsWith("//")) uri else "/"
        exchange.response.statusCode = org.springframework.http.HttpStatus.FOUND
        exchange.response.headers.location = URI.create(safeUri)
        return exchange.response.setComplete()
    }
}
