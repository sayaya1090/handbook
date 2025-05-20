package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeService
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@RestController
class TypeController(private val svc: TypeService) {
    @GetMapping("/workspace/{workspace}/types/{type}", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun findByName(@PathVariable workspace: UUID, @PathVariable type: String,
                   @RequestParam("version", required = false) version: String?
    ): Mono<EntityModel<Type>> = TODO()
    @GetMapping("/workspace/{workspace}/types", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun find(
        @PathVariable workspace: UUID,
        @RequestParam("effect_date_time", required = false) effectDateTime: Instant?,
        @RequestParam("expire_date_time", required = false) expireDateTime: Instant?
    ): Flux<EntityModel<Type>> = svc.findByRange(workspace, effectDateTime, expireDateTime)
        .flatMap { type -> buildTypeEntityModelWithLinks(type, workspace) }


    private fun buildTypeEntityModelWithLinks(type: Type, workspace: UUID): Mono<EntityModel<Type>> {
        val selfLinkMono = linkTo(methodOn(TypeController::class.java).findByName(workspace, type.id, type.version))
            .withSelfRel()
            .toMono()
        val prevLinkMono = if (type.prev != null) linkTo(
            methodOn(TypeController::class.java).findByName(workspace, type.id, type.prev)
        ).withRel("prev-version").toMono()
        else Mono.empty()

        val nextLinkMono = if (type.next != null) linkTo(
            methodOn(TypeController::class.java).findByName(workspace, type.id, type.next)
        ).withRel("next-version").toMono()
        else Mono.empty()

        return Flux.merge(listOf(
            selfLinkMono, prevLinkMono, nextLinkMono
        )).collectList().map { links ->
            EntityModel.of(type, links)
        }
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}