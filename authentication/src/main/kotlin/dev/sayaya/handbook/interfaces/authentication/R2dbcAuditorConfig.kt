package dev.sayaya.handbook.interfaces.authentication

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import java.time.Duration
import java.util.*

@Configuration
@ConditionalOnMissingBean(ReactiveAuditorAware::class)
@EnableR2dbcAuditing
class R2dbcAuditorConfig {
    @Bean fun auditorProvider(): ReactiveAuditorAware<UUID> = ReactiveAuditorAware {
        ReactiveSecurityContextHolder.getContext()
            .timeout(Duration.ofSeconds(1))
            .map { obj: SecurityContext -> obj.authentication }
            .filter { obj: Authentication -> obj.isAuthenticated }
            .mapNotNull { obj: Authentication ->
                val principal = obj.principal
                when (principal) {
                    is String -> UUID.fromString(principal)
                    is UUID -> principal
                    else -> null
                }
            }
    }
}