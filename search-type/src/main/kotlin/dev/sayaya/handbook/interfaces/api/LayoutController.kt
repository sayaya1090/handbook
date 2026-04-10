package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.TypeLayout
import dev.sayaya.handbook.usecase.LayoutService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.util.*

@RestController
@RequestMapping("/workspace/{workspace}/layouts")
class LayoutController(private val svc: LayoutService) {

    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun findByWorkspace(@PathVariable workspace: UUID): Flux<TypeLayout> = svc.findByWorkspace(workspace)
}
