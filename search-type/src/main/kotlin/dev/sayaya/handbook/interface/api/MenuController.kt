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
            .title("types")
            .supportingText("Define Types, Properties, and Relations")
            .order("B")
            .icon("fa-cubes")
            .iconType("sharp")
            .script("js/type.nocache.js")
            .tools(
                Tool.builder().title("View as Graph").order("BE").icon("fa-diagram-project").iconType("sharp").build(),
                Tool.builder().title("View as Calendar").order("BH").icon("fa-calendar-range").iconType("sharp").build()
            ).build()
    }
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(request: ServerHttpRequest): Flux<Menu> = Flux.just(MENU)
}