package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/** API 게이트웨이 Spring Boot 애플리케이션 진입점. 라우팅, 메뉴 집계, 서비스 디스커버리를 담당한다. */
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}