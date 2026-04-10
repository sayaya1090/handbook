package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LoginApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<LoginApplication>(*args)
        }
    }
}
