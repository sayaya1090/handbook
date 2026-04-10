package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/workspace/{workspace}/types")
class TypeController(private val svc: TypeService) {

    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun find(
        @PathVariable workspace: UUID,
        @RequestParam("effect_date_time", required = false) effectDateTime: Instant?,
        @RequestParam("expire_date_time", required = false) expireDateTime: Instant?,
    ): Flux<Type> = svc.findByRange(workspace, effectDateTime, expireDateTime)
}
