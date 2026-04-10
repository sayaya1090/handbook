package dev.sayaya.handbook.interfaces.authentication

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.context.ContextConfiguration

@WebFluxTest(properties = [
    "spring.security.authentication.header=Authorization",
    "spring.security.authentication.refresh=New-Refresh-Token",
    "spring.security.authentication.jwt-secret=test-secret",
]) @ContextConfiguration(classes = [AuthenticationConfigTest.Companion.TestConfig::class])
@ApplyExtension(SpringExtension::class)
internal class AuthenticationConfigTest(
    private val authenticationConfig: AuthenticationConfig
) : StringSpec({
    "header 프로퍼티가 올바르게 로드된다" {
        authenticationConfig.header shouldBe "Authorization"
    }
    "refresh 프로퍼티의 기본값을 재정의할 수 있다" {
        authenticationConfig.refresh shouldBe "New-Refresh-Token"
    }
    "jwtSecret 프로퍼티가 올바르게 로드된다" {
        authenticationConfig.jwtSecret shouldBe "test-secret"
    }
}) {
    companion object {
        @SpringBootConfiguration
        @EnableConfigurationProperties(AuthenticationConfig::class)
        class TestConfig
    }
}
