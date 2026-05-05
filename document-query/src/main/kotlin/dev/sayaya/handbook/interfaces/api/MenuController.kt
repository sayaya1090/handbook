package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.domain.SessionStateKind
import dev.sayaya.handbook.domain.Tool
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.security.Principal

/**
 * 문서 도메인의 `/menus` 공급자.
 */
@RestController
@Tag(name = "Document Menu", description = "Document 도메인의 /menus 공급자.")
class MenuController {
    companion object {
        val DOCUMENTS_MENU: Menu = Menu.builder()
            .title("documents")
            .supportingText("document.menu.supporting")
            .order("A")
            .icon("fa-database")
            .iconType("sharp")
            .script("/js/data/data.nocache.js")
            .tools(
                Tool.builder().title("document_editor").order("AE").icon("fa-table").iconType("sharp").build(),
            ).url("/workspaces/{workspaceId}/documents").urls("^/workspaces/\\{workspaceId\\}/documents$")
            .allowedSessionStates(SessionStateKind.IN_WORKSPACE)
            .build()

        val DASHBOARD_MENU: Menu = Menu.builder()
            .title("dashboard")
            .supportingText("dashboard.menu.supporting")
            .order("C")
            .icon("fa-chart-line")
            .iconType("sharp")
            .script("/js/dashboard/dashboard.nocache.js")
            .url("/workspaces/{workspaceId}/dashboard").urls("^/workspaces/\\{workspaceId\\}/dashboard$")
            .allowedSessionStates(SessionStateKind.IN_WORKSPACE)
            .build()
    }

    @Operation(summary = "Fetch document and dashboard menu entries")
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(principal: Principal?): Flux<Menu> =
        if (principal == null) Flux.empty()
        else Flux.just(DOCUMENTS_MENU, DASHBOARD_MENU)
}
