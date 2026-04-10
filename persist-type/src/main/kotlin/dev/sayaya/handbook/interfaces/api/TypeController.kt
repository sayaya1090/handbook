package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/workspace/{workspace}/types")
class TypeController(private val typeService: TypeService) {

    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun findByPeriod(
        @PathVariable workspace: UUID,
        @RequestParam("effect_date_time") effectDateTime: Instant,
        @RequestParam("expire_date_time") expireDateTime: Instant,
    ): Flux<Type> = typeService.findByPeriod(workspace, effectDateTime, expireDateTime)

    @PutMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun save(
        @PathVariable workspace: UUID,
        @RequestBody types: List<Type>,
    ): Flux<Type> = typeService.save(workspace, types)

    @DeleteMapping(consumes = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable workspace: UUID,
        @RequestBody types: List<Type>,
    ): Mono<Void> = typeService.delete(workspace, types)
}
