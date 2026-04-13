package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/** 이벤트 브로드캐스터 Spring Boot 애플리케이션 진입점. Kafka 이벤트를 SSE로 실시간 브로드캐스트한다. */
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}