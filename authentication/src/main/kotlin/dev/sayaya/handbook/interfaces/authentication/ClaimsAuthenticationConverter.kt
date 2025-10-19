package dev.sayaya.handbook.interfaces.authentication

import io.jsonwebtoken.Claims
import org.springframework.security.core.Authentication

/**
 * JWT Claims를 Spring Security Authentication 객체로 변환하는 전략 인터페이스
 *
 * JWT 토큰에서 파싱된 Claims 정보를 받아서
 * 애플리케이션에 맞는 Authentication 구현체로 변환합니다.
 *
 * @see UserAuthenticationConverter
 */
fun interface ClaimsAuthenticationConverter {
    /**
     * JWT Claims를 Authentication 객체로 변환
     *
     * @param claims JWT 토큰에서 파싱된 Claims
     * @param token 원본 JWT 토큰 문자열
     * @return 변환된 Authentication 객체
     */
    fun convert(claims: Claims, token: String): Authentication
}