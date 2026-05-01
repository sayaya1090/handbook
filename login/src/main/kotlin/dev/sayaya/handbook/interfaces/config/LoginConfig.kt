package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.usecase.TokenFactory
import dev.sayaya.handbook.usecase.TokenPublisher
import dev.sayaya.handbook.usecase.UserRepository
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper

/**
 * 로그인 모듈의 Spring Bean 설정.
 *
 * **책임:** usecase 계층의 [TokenFactory]와 [TokenPublisher]를 Bean으로 등록하고,
 * [TokenFactoryConfig], [AuthenticationUrlConfig] 프로퍼티를 활성화한다.
 *
 * **의존관계:**
 * - [TokenFactoryConfig] — JWT 서명키, 만료 시간 설정
 * - [AuthenticationUrlConfig] — 인증 후 리다이렉트 URL 설정
 */
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
