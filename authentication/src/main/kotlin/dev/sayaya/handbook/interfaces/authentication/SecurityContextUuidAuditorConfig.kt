package dev.sayaya.handbook.interfaces.authentication

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import java.time.Duration
import java.util.*

/**
 * Spring Data R2DBC Auditing을 위한 Auditor 제공 자동 구성
 *
 * Security Context에서 현재 인증된 사용자의 UUID를 추출하여
 * 엔티티의 생성자 및 수정자 필드에 자동으로 설정합니다.
 * 
 * 다음 조건을 모두 만족할 때만 활성화됩니다:
 * - R2DBC 관련 클래스가 classpath에 있을 것
 * - ConnectionFactory 빈이 존재할 것 (실제 R2DBC 설정이 되어 있음을 의미)
 * - 다른 ReactiveAuditorAware 빈이 없을 것
 */
@AutoConfiguration
@ConditionalOnClass(R2dbcEntityTemplate::class, ConnectionFactory::class)
@ConditionalOnBean(ConnectionFactory::class)
@ConditionalOnMissingBean(ReactiveAuditorAware::class)
@EnableR2dbcAuditing
class SecurityContextUuidAuditorConfig {
    @Bean fun auditorProvider(): ReactiveAuditorAware<UUID> = ReactiveAuditorAware {
        ReactiveSecurityContextHolder.getContext()
            .timeout(Duration.ofSeconds(1))
            .map { obj: SecurityContext -> obj.authentication }
            .filter { obj: Authentication -> obj.isAuthenticated }
            .mapNotNull { obj: Authentication ->
                when (val principal = obj.principal) {
                    is String -> UUID.fromString(principal)
                    is UUID -> principal
                    else -> null
                }
            }
    }
}