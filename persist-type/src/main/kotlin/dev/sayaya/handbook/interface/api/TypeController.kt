package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.TypeWithLayout
import dev.sayaya.handbook.usecase.type.TypeService
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@RestController
class TypeController(private val svc: TypeService) {
    @PostMapping(value = ["/workspace/{workspace}/types"])
    fun save(@AuthenticationPrincipal principal: Principal, @PathVariable workspace: UUID, @RequestBody types: List<TypeWithLayout>): Mono<Void> = svc.save(principal, workspace,types).then()

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