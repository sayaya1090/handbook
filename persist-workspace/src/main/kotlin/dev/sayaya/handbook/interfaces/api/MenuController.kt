package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.domain.Tool
import dev.sayaya.handbook.domain.ToolFunction
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class MenuController {
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
                Tool.builder().title("permissions").order("S9").icon("fa-key").iconType("sharp").build(),
            ).url("^workspaces")
            .build()
    }

    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(): Flux<Menu> = Flux.just(MENU)
}
