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
 * 타입 도메인의 `/menus` 공급자.
 */
@RestController
@Tag(name = "Type Menu", description = "Type 도메인의 /menus 공급자.")
class MenuController {
    companion object {
        val MENU: Menu = Menu.builder()
            .title("types")
            .supportingText("type.menu.supporting")
            .order("B")
            .icon("fa-cubes")
            .iconType("sharp")
            .script("/js/type/type.nocache.js")
            .tools(
                Tool.builder().title("type_canvas").order("BE").icon("fa-diagram-project").iconType("sharp").build(),
            ).url("/workspaces/{workspaceId}/types").urls("^/workspaces/\\{workspaceId\\}/types$")
            .allowedSessionStates(SessionStateKind.IN_WORKSPACE)
            .build()
    }

    @Operation(summary = "Fetch type menu entries")
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(principal: Principal?): Flux<Menu> =
        if (principal == null) Flux.empty()
        else Flux.just(MENU)
}
