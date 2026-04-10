package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PersistTypeApplication

fun main(args: Array<String>) {
    runApplication<PersistTypeApplication>(*args)
}
