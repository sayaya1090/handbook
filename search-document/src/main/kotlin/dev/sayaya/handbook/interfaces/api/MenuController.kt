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
        val DOCUMENTS_MENU: Menu = Menu.builder()
            .title("documents")
            .supportingText("Define Master Data")
            .order("A")
            .icon("fa-database")
            .iconType("sharp")
            .script("js/data/data.nocache.js")
            .tools(
                Tool.builder().title("View as Table").order("AE").icon("fa-table").iconType("sharp").build(),
            ).url("/workspace/{workspaceId}/documents").urls("^/workspace/\\{workspaceId\\}/documents$")
            .allowedSessionStates(SessionStateKind.IN_WORKSPACE)
            .build()

        val DASHBOARD_MENU: Menu = Menu.builder()
            .title("dashboard")
            .supportingText("Monitor Quality and Activity")
            .order("C")
            .icon("fa-chart-line")
            .iconType("sharp")
            .script("js/dashboard/dashboard.nocache.js")
            .url("/workspace/{workspaceId}/dashboard").urls("^/workspace/\\{workspaceId\\}/dashboard$")
            .allowedSessionStates(SessionStateKind.IN_WORKSPACE)
            .build()
    }

    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(): Flux<Menu> = Flux.just(DOCUMENTS_MENU, DASHBOARD_MENU)
}
