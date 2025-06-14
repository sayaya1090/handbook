package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.client.domain.Menu
import dev.sayaya.handbook.client.domain.Tool
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
internal class MenuController {
    companion object {
        val MENU: Menu = Menu.builder()
            .title("workspaces")
            .supportingText("Name, Group, and Permission")
            .order("S")
            .icon("fa-briefcase")
            .iconType("sharp")
            .script("js/workspace.nocache.js")
            .bottom(true)
            .tools(
                Tool.builder().title("workspace info").order("S1").icon("fa-information").iconType("sharp").build(),
                Tool.builder().title("groups").order("S5").icon("fa-users-gear").iconType("sharp").build(),
                Tool.builder().title("permissions").order("S9").icon("fa-key").iconType("sharp").build()
            ).url("^workspaces")
            .build()
    }
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(request: ServerHttpRequest): Flux<Menu> = Flux.just(MENU)
}