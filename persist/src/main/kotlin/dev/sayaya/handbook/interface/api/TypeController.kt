package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeService
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
class TypeController(private val svc: TypeService) {
    @PutMapping(value = ["/types"])
    fun save(@RequestBody type: Type): Mono<Void> = svc.save(type).then()

    @ExceptionHandler(DuplicateKeyException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateKeyException(ex: DuplicateKeyException): Mono<String> = Mono.justOrEmpty(ex.localizedMessage)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}