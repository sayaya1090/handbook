package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.authentication.AuthenticationConfig
import dev.sayaya.handbook.interfaces.authentication.JwtAuthenticationConverter
import dev.sayaya.handbook.interfaces.authentication.JwtAuthenticationManager
import dev.sayaya.handbook.interfaces.authentication.NoWwwAuthenticateEntryPoint
import dev.sayaya.handbook.usecase.TokenPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpCookie
import org.springframework.http.ResponseCookie
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class LoginSecurityConfig(
    private val authConfig: AuthenticationConfig,
    private val urlConfig: AuthenticationUrlConfig,
    private val tokenPublisher: TokenPublisher,
    private val tokenFactoryConfig: TokenFactoryConfig,
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
            headers { frameOptions { mode = XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN } }
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
                sendAuthenticationCookie(webFilterExchange.exchange, token)
                    .then(redirect(webFilterExchange.exchange, urlConfig.loginRedirectUri))
            }
        }
    }

    private fun logoutSuccessHandler(): ServerLogoutSuccessHandler {
        return ServerLogoutSuccessHandler { webFilterExchange, _ ->
            clearAuthenticationCookie(webFilterExchange.exchange)
                .then(redirect(webFilterExchange.exchange, urlConfig.logoutRedirectUri))
        }
    }

    fun sendAuthenticationCookie(exchange: ServerWebExchange, token: String): Mono<Void> {
        val cookie = ResponseCookie.from(authConfig.header, token)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(tokenFactoryConfig.duration)
            .build()
        exchange.response.addCookie(cookie)
        return Mono.empty()
    }

    private fun clearAuthenticationCookie(exchange: ServerWebExchange): Mono<Void> {
        val cookie = ResponseCookie.from(authConfig.header, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build()
        exchange.response.addCookie(cookie)
        return Mono.empty()
    }

    private fun redirect(exchange: ServerWebExchange, uri: String): Mono<Void> {
        exchange.response.statusCode = org.springframework.http.HttpStatus.FOUND
        exchange.response.headers.location = URI.create(uri)
        return exchange.response.setComplete()
    }
}
