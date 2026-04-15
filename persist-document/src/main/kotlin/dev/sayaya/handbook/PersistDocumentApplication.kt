package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PersistDocumentApplication

fun main(args: Array<String>) {
    runApplication<PersistDocumentApplication>(*args)
}
