package dev.sayaya.handbook.usecase

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix="spring.security.authentication.jwt")
class TokenFactoryConfig {
    lateinit var signatureAlgorithm: String
    var duration: Long = -1L
    lateinit var publisher: String
    lateinit var client: String
}