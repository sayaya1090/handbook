package dev.sayaya.handbook.domain

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest

@WebFluxTest(properties = [
    "spring.security.authentication.jwt.secret=my-test-secret"
]) @ApplyExtension(SpringExtension::class)
internal class TokenConfigTest(
    private val tokenConfig: TokenConfig
) : StringSpec({
    "secret 프로퍼티를 올바르게 로드한다" {
        tokenConfig.secret shouldBe "my-test-secret"
    }
}) {
    companion object {
        @SpringBootConfiguration
        @EnableConfigurationProperties(TokenConfig::class)
        class TestConfig
    }
}