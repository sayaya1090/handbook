package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.domain.SessionStateKind
import dev.sayaya.handbook.domain.Tool
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class MenuController {
    companion object {
        val MENU: Menu = Menu.builder()
            .title("types")
            .supportingText("Define Types, Properties, and Relations")
            .order("B")
            .icon("fa-cubes")
            .iconType("sharp")
            .script("/js/type/type.nocache.js")
            .tools(
                Tool.builder().title("View as Graph").order("BE").icon("fa-diagram-project").iconType("sharp").build(),
            ).url("/workspace/{workspaceId}/types").urls("^/workspace/\\{workspaceId\\}/types$")
            .allowedSessionStates(SessionStateKind.IN_WORKSPACE)
            .build()
    }

    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(): Flux<Menu> = Flux.just(MENU)
}
