package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.authentication.JwtAuthenticationConverter
import dev.sayaya.handbook.interfaces.authentication.JwtAuthenticationManager
import dev.sayaya.handbook.interfaces.authentication.NoWwwAuthenticateEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter

//
// search-workspace Spring Security 설정.
//
// 역할:
//   - JWT 인증 필터 등록 + 경로별 접근 권한 정의.
//   - authentication 모듈이 제공하는 기본 체인(ConditionalOnMissingBean) 을 대체.
//
// permitAll 공개 경로:
//   - /menus                            gateway MenuService 집계 엔드포인트.
//                                       비인증 호출도 허용 (응답 본문에서 principal 유무로 분기)
//   - /v3/api-docs, /v3/api-docs/**     OpenAPI 스펙 디스커버리 (외부 AI Tool Use)
//   - /swagger-ui.html, /swagger-ui/**  swagger UI
//   - /actuator/health, /actuator/info  프로브
//
// 인증 필요:
//   - /workspaces, /workspaces/**       JWT 검증 후 principal 기반 응답
//
@Configuration
class SearchWorkspaceSecurityConfig {
    @Bean
    fun searchWorkspaceSecurityFilterChain(
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
            authorizeExchange {
                authorize("/menus", permitAll)
                authorize("/v3/api-docs", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/actuator/health/**", permitAll)
                authorize("/actuator/info", permitAll)
                authorize("/actuator/**", authenticated)
                authorize(anyExchange, authenticated)
            }
        }
    }
}
