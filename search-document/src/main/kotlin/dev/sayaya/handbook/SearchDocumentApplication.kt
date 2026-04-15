package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SearchDocumentApplication

fun main(args: Array<String>) {
    runApplication<SearchDocumentApplication>(*args)
}
