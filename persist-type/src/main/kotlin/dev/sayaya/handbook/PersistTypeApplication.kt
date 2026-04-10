package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/** persist-type 모듈 Spring Boot 애플리케이션 진입점. 타입 스키마의 CUD 및 Kafka 이벤트 발행을 담당한다. */
@SpringBootApplication
class PersistTypeApplication

fun main(args: Array<String>) {
    runApplication<PersistTypeApplication>(*args)
}
