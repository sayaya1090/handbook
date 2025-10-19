package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.usecase.authentication.UserAuthentication
import io.jsonwebtoken.Claims
import org.springframework.security.core.Authentication
import java.time.ZoneId
import java.util.Date

/**
 * JWT Claims를 [UserAuthentication] 객체로 변환하는 컨버터
 *
 * JWT 토큰의 Claims 정보를 파싱하여 애플리케이션의 사용자 인증 객체로 변환합니다.
 *
 * @see ClaimsAuthenticationConverter
 * @see UserAuthentication
 */
class UserAuthenticationConverter : ClaimsAuthenticationConverter {
    override fun convert(claims: Claims, token: String): Authentication = UserAuthentication(
        id = claims.id,
        username = claims.get("name", String::class.java),
        issuer = claims.issuer,
        issuedDateTime = claims.issuedAt.toLocalDateTime(),
        notBeforeDateTime = claims.notBefore.toLocalDateTime(),
        expireDateTime = claims.expiration.toLocalDateTime(),
        token = token
    )
    companion object {
        private fun Date.toLocalDateTime() = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
}