package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.usecase.LayoutService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
class LayoutController(private val svc: LayoutService) {
    @GetMapping("/workspace/{workspace}/layouts", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun layouts(@PathVariable workspace: UUID): Flux<Layout> = svc.findAll(workspace)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}