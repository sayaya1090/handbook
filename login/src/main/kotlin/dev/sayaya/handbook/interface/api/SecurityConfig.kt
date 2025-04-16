package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.interfaces.authentication.AuthenticationConfig
import dev.sayaya.handbook.interfaces.authentication.JwtAuthenticationConverter
import dev.sayaya.handbook.interfaces.authentication.JwtAuthenticationManager
import dev.sayaya.handbook.usecase.TokenFactoryConfig
import dev.sayaya.handbook.usecase.TokenPublisher
import org.springframework.boot.web.server.Cookie.SameSite.LAX
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
class SecurityConfig(
    private val authConfig: AuthenticationConfig,
    private val urlConfig: AuthenticationUrlConfig,
    private val tokenPublisher: TokenPublisher,
    private val tokenConfig: TokenFactoryConfig,
    private val jwtAuthenticationConverter: JwtAuthenticationConverter,
    jwtAuthenticationManager: JwtAuthenticationManager
) {
    private val authenticationWebFilter = AuthenticationWebFilter(jwtAuthenticationManager)
    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http {
        csrf { disable() }
        httpBasic { disable() }
        formLogin { disable() }
        headers { frameOptions { mode = XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN } }
        oauth2Login {
            securityContextRepository = NoOpServerSecurityContextRepository.getInstance()
            authenticationSuccessHandler = ServerAuthenticationSuccessHandler { filter, authentication ->
                val exchange = filter.exchange
                val provider = exchange.request.path.value().split("/").last()
                val principal = authentication.principal as OAuth2User
                tokenPublisher.publish(provider, principal).flatMap { token -> exchange.sendAuthenticationCookie(token) }
            }
        }
        logout {
            logoutUrl = "/oauth2/logout"
            logoutHandler = DelegatingServerLogoutHandler(SecurityContextServerLogoutHandler(), WebSessionServerLogoutHandler())
            logoutSuccessHandler = ServerLogoutSuccessHandler { exchange, _ ->
                exchange.exchange.clearAuthenticationCookie()
            }
        }
        exceptionHandling {
            authenticationEntryPoint = ServerAuthenticationEntryPoint { exchange, e ->
                e.printStackTrace()
                Mono.fromRunnable { exchange.response.statusCode = HttpStatus.UNAUTHORIZED } }
            accessDeniedHandler = ServerAccessDeniedHandler { exchange, e ->
                e.printStackTrace()
                Mono.fromRunnable { exchange.response.statusCode = HttpStatus.FORBIDDEN } }
        }
        authenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter)
        addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        anonymous { }
    }
    private fun ServerWebExchange.sendAuthenticationCookie(token: String): Mono<Void> {
        response.addCookie(ResponseCookie.from(authConfig.header, token).path("/").httpOnly(true).secure(true).maxAge(tokenConfig.duration).sameSite(LAX.attributeValue()).build())
        response.statusCode = HttpStatus.FOUND
        response.headers.location = URI.create(urlConfig.loginRedirectUri)
        return response.setComplete()
    }
    private fun ServerWebExchange.clearAuthenticationCookie(): Mono<Void> {
        response.addCookie(ResponseCookie.from(authConfig.header).path("/").httpOnly(true).secure(true).maxAge(0).build())
        response.statusCode = HttpStatus.FOUND
        response.headers.location = URI.create(urlConfig.logoutRedirectUri)
        return response.setComplete()
    }
}