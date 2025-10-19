package dev.sayaya.handbook.interfaces.authentication

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest

@EnableConfigurationProperties(AuthenticationConfig::class)
@WebFluxTest(properties = [
    "spring.security.authentication.header=Authorization",
    "spring.security.authentication.refresh=New-Refresh-Token"
]) @ApplyExtension(SpringExtension::class )
internal class AuthenticationConfigTest (
    private val authenticationConfig: AuthenticationConfig
) : StringSpec({
    "설정 프로퍼티가 올바르게 로드된다" {
        authenticationConfig.header shouldBe "Authorization"
    }
    "refresh 프로퍼티의 기본값을 재정의할 수 있다" {
        authenticationConfig.refresh shouldBe "New-Refresh-Token"
    }
})