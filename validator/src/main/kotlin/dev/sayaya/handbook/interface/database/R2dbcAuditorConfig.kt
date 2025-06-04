package dev.sayaya.handbook.`interface`.database

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing

@Configuration
@EnableR2dbcAuditing
class R2dbcAuditorConfig {
    init {
        println("R2DBC Auditor Config Initialized")
    }
}
