package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class UserController {
    @GetMapping(value = ["/user"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun user(@AuthenticationPrincipal authentication: UserAuthentication): Mono<UserResponse> {
        return Mono.just(
            UserResponse(
                id = authentication.id,
                name = authentication.username,
            )
        )
    }

    data class UserResponse(
        val id: String?,
        val name: String,
    )
}
