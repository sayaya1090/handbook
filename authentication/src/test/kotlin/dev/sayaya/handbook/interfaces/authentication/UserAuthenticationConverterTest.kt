package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.usecase.authentication.UserAuthentication
import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

internal class UserAuthenticationConverterTest : StringSpec({

    val converter = UserAuthenticationConverter()

    "convert 메소드는 Claims 객체를 UserAuthentication 객체로 올바르게 변환한다" {
        // Arrange
        val now = Instant.now()
        val claims = Jwts.claims()
            .id("user-id-123")
            .issuer("test-issuer")
            .issuedAt(Date.from(now))
            .notBefore(Date.from(now))
            .expiration(Date.from(now.plusSeconds(3600)))
            .add("name", "test-user")
            .build()
        val token = "test.jwt.token"

        // Act
        val result = converter.convert(claims, token)

        // Assert
        result.shouldBeInstanceOf<UserAuthentication>()

        val auth = result as UserAuthentication
        auth.id shouldBe "user-id-123"
        auth.username shouldBe "test-user"
        auth.issuer shouldBe "test-issuer"
        auth.credentials shouldBe token

        val systemZone = ZoneId.systemDefault()
        val truncatedNow = now.truncatedTo(ChronoUnit.SECONDS)
        auth.issuedDateTime shouldBe truncatedNow.atZone(systemZone).toLocalDateTime()
        auth.notBeforeDateTime shouldBe truncatedNow.atZone(systemZone).toLocalDateTime()
        auth.expireDateTime shouldBe truncatedNow.plusSeconds(3600).atZone(systemZone).toLocalDateTime()
    }
})