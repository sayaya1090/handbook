package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@RestController
class TypeController(private val svc: TypeService) {
    @GetMapping("/workspace/{workspace}/layouts", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun layouts(@PathVariable workspace: UUID): Flux<Layout> = svc.findAll(workspace)
    @GetMapping("/workspace/{workspace}/types", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun find(@PathVariable workspace: UUID,
             @RequestParam("effect_date_time") effectDateTime: Instant,
             @RequestParam("expire_date_time", required=false) expireDateTime: Instant?
    ): Flux<Type> = svc.findByRange(workspace, effectDateTime, expireDateTime?:effectDateTime)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}