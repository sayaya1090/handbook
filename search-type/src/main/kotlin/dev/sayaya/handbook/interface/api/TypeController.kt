package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.TypeWithLayout
import dev.sayaya.handbook.usecase.LayoutService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@RestController
class TypeController(private val svc: LayoutService) {
    @GetMapping("/workspace/{workspace}/layouts", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun layouts(@PathVariable workspace: UUID): Flux<Layout> = svc.findAll(workspace)
    @GetMapping("/workspace/{workspace}/types", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun find(@PathVariable workspace: UUID, @RequestParam("basetime") baseTime: Instant): Flux<TypeWithLayout> = svc.findByBaseTime(workspace, baseTime)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}