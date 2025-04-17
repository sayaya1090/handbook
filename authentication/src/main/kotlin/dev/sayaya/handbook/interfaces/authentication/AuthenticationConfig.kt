package dev.sayaya.handbook.interfaces.authentication

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix="spring.security.authentication")
class AuthenticationConfig {
    lateinit var header: String
    var refresh: String = "Refresh"
}