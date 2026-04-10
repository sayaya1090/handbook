package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.TypeLayout
import dev.sayaya.handbook.usecase.LayoutService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/workspace/{workspace}/layouts")
class LayoutController(private val layoutService: LayoutService) {

    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun findByWorkspace(@PathVariable workspace: UUID): Flux<TypeLayout> =
        layoutService.findByWorkspace(workspace)

    @PutMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun save(
        @PathVariable workspace: UUID,
        @RequestBody layout: TypeLayout,
    ): Mono<TypeLayout> = layoutService.save(workspace, layout)
}
