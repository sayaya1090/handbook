package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/** 로그인 모듈 Spring Boot 애플리케이션 진입점. OAuth2 인증 및 JWT 발급을 담당한다. */
@SpringBootApplication
class LoginApplication

fun main(args: Array<String>) {
    runApplication<LoginApplication>(*args)
}
