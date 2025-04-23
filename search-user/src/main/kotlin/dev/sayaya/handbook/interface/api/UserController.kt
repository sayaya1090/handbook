package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.usecase.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
class UserController(private val svc: UserService) {
    @GetMapping("/user", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun findCurrentUser(@AuthenticationPrincipal principal: Principal): Mono<User> = svc.find(principal)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}