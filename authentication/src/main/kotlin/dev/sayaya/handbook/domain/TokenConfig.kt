package dev.sayaya.handbook.domain

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * JWT 토큰 관련 설정을 관리하는 구성 클래스
 *
 * application.yml 또는 application.properties 파일의
 * `spring.security.authentication.jwt` 접두사로 시작하는 속성들을 바인딩합니다.
 *
 * @property secret JWT 토큰 서명 및 검증에 사용되는 비밀 키 (PEM 형식의 공개키 또는 개인키)
 */
@ConfigurationProperties(prefix="spring.security.authentication.jwt")
class TokenConfig {
    lateinit var secret: String
}