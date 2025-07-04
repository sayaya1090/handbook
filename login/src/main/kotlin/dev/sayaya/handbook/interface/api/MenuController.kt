package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.client.domain.Menu
import dev.sayaya.handbook.client.domain.Tool
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.security.Principal

@RestController
internal class MenuController {
    companion object {
        val SIGN_IN: Menu = Menu.builder()
            .title("sign in")
            .supportingText("Authentication for service use")
            .order("0")
            .icon("fa-right-to-bracket")
            .iconType("sharp")
            .script("js/login/login.nocache.js")
            .tools(
                Tool.builder().title("sign in").order("Z-1").icon("fa-right-to-bracket").iconType("sharp").build(),
            ).url("login")
            .build()
        val SIGN_OUT: Menu = Menu.builder()
            .title("sign out")
            .supportingText("Safely end session")
            .order("Z")
            .icon("fa-left-from-bracket")
            .iconType("sharp")
            .script("js/logout/logout.nocache.js")
            .bottom(true)
            .tools(
                Tool.builder().title("sign out").order("Z-1").icon("fa-left-from-bracket").iconType("sharp").build(),
            ).url("logout")
            .build()
    }
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(principal: Principal?): Flux<Menu> =
        if(principal==null || principal is AnonymousAuthenticationToken) Flux.just(SIGN_IN).doOnNext { println(it) }
        else Flux.just(SIGN_OUT).doOnNext { println(it) }
}