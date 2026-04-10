package dev.sayaya.handbook.interfaces.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
class TokenFactoryConfig {
    lateinit var secret: String
    var duration: Long = 3600
    var publisher: String = "handbook"
    var client: String = "handbook"
}
