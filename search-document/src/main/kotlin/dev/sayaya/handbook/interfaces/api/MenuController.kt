package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
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
            .title("documents")
            .supportingText("Define Master Data")
            .order("A")
            .icon("fa-database")
            .iconType("sharp")
            .script("js/data/data.nocache.js")
            .tools(
                Tool.builder().title("View as Table").order("AE").icon("fa-table").iconType("sharp").build(),
            ).url("^documents")
            .build()
    }

    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(): Flux<Menu> = Flux.just(MENU)
}
