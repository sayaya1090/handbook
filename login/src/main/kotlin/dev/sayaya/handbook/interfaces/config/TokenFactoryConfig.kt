package dev.sayaya.handbook.interfaces.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * JWT 토큰 생성에 필요한 설정을 `jwt.*` 프로퍼티에서 바인딩한다.
 *
 * **책임:** RSA 개인키(PEM), 토큰 유효 기간, 발행자/클라이언트 식별자를 제공한다.
 *
 * @property secret PEM 형식의 RSA 개인키 문자열
 * @property duration 토큰 유효 시간 (초 단위, 기본값: 3600)
 * @property publisher JWT issuer 클레임 값 (기본값: "handbook")
 * @property client 클라이언트 식별자 (기본값: "handbook")
 */
@ConfigurationProperties(prefix = "jwt")
class TokenFactoryConfig {
    lateinit var secret: String
    var duration: Long = 3600
    var publisher: String = "handbook"
    var client: String = "handbook"
}
