package dev.sayaya.handbook.interfaces.config

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.usecase.TokenFactory
import dev.sayaya.handbook.usecase.TokenPublisher
import dev.sayaya.handbook.usecase.UserRepository
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TokenFactoryConfig::class, AuthenticationUrlConfig::class)
class LoginConfig {
    @Bean
    fun tokenFactory(config: TokenFactoryConfig, objectMapper: ObjectMapper) =
        TokenFactory(config, objectMapper)

    @Bean
    fun tokenPublisher(userRepository: UserRepository, tokenFactory: TokenFactory) =
        TokenPublisher(userRepository, tokenFactory)
}
