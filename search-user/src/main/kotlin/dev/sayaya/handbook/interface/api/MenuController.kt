package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.client.domain.Menu
import dev.sayaya.handbook.client.domain.Tool
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.security.Principal

@RestController
internal class MenuController {
    companion object {
        val MENU: Menu = Menu.builder()
            .title("user info")
            .supportingText("View/Edit User Information")
            .order("U")
            .icon("fa-user")
            .iconType("sharp")
            .script("js/user.nocache.js")
            .bottom(true)
            .tools(
                Tool.builder().title("user info").order("U1").icon("fa-information").iconType("sharp").build()
            ).url("^users").build()
    }
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(principal: Principal?, request: ServerHttpRequest): Flux<Menu> =
        if(principal==null || principal is AnonymousAuthenticationToken) Flux.empty()
        else Flux.just(MENU)
}