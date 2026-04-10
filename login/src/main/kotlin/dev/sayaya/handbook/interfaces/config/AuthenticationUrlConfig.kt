package dev.sayaya.handbook.interfaces.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.url")
class AuthenticationUrlConfig {
    var loginRedirectUri: String = "/"
    var logoutRedirectUri: String = "/"
}
