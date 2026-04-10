package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.security.Principal

@RestController
class MenuController {
    companion object {
        val SIGN_IN: Menu = Menu.builder()
            .title("sign in")
            .order("Z")
            .icon("fa-right-to-bracket")
            .iconType("sharp")
            .script("js/login.nocache.js")
            .bottom(true)
            .build()

        val SIGN_OUT: Menu = Menu.builder()
            .title("sign out")
            .order("Z")
            .icon("fa-right-from-bracket")
            .iconType("sharp")
            .script("js/logout.nocache.js")
            .bottom(true)
            .build()
    }

    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(principal: Principal?): Flux<Menu> {
        return if (principal == null) Flux.just(SIGN_IN)
        else Flux.just(SIGN_OUT)
    }
}
