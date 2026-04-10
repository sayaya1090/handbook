package dev.sayaya.handbook.interfaces.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * OAuth2 인증 후 리다이렉트 URL 설정.
 *
 * **책임:** 로그인/로그아웃 성공 후 리다이렉트할 URI를 `auth.url.*` 프로퍼티에서 바인딩한다.
 *
 * @property loginRedirectUri 로그인 성공 후 리다이렉트 URI (기본값: "/")
 * @property logoutRedirectUri 로그아웃 성공 후 리다이렉트 URI (기본값: "/")
 */
@ConfigurationProperties(prefix = "auth.url")
class AuthenticationUrlConfig {
    var loginRedirectUri: String = "/"
    var logoutRedirectUri: String = "/"
}
