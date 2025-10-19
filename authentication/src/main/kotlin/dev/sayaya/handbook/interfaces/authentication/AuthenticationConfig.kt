package dev.sayaya.handbook.interfaces.authentication

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 인증 관련 설정을 관리하는 구성 클래스
 *
 * application.yml 또는 application.properties 파일의
 * `spring.security.authentication` 접두사로 시작하는 속성들을 바인딩합니다.
 *
 * @property header JWT 토큰을 저장하는 쿠키의 이름
 * @property refresh 리프레시 토큰을 저장하는 쿠키의 이름 (기본값: "Refresh")
 */
@ConfigurationProperties(prefix="spring.security.authentication")
class AuthenticationConfig {
    lateinit var header: String
    var refresh: String = "Refresh"
}