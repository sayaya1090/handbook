package dev.sayaya.handbook.`interface`.database

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import reactor.core.publisher.Mono
import java.util.*

@Configuration
@EnableR2dbcAuditing
class R2dbcAuditorConfig {
    @Bean fun auditorProvider(): ReactiveAuditorAware<UUID> = ReactiveAuditorAware {
        Mono.just(UUID.fromString("93951bc3-be1e-4fc8-865f-d6376ac3e87b"))
    }
}