package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PersistWorkspaceApplication

fun main(args: Array<String>) {
    runApplication<PersistWorkspaceApplication>(*args)
}
