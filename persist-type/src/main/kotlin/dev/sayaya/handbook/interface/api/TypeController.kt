package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeService
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@RestController
class TypeController(private val svc: TypeService) {
    private val logger = LoggerFactory.getLogger(TypeController::class.java)
    @PutMapping(value = ["/workspace/{workspace}/types"])
    @Transactional
    fun save(@AuthenticationPrincipal principal: Principal, @PathVariable workspace: UUID, @RequestBody types: List<Type>): Mono<Void> = svc.save(principal, workspace, types).doOnError { error ->
        logger.error("Error saving types for workspace {}: {}", workspace, error.message, error)
    }.then()

    @DeleteMapping(value = ["/workspace/{workspace}/types"])
    @Transactional
    fun delete(@AuthenticationPrincipal principal: Principal, @PathVariable workspace: UUID, @RequestBody types: List<Type>): Mono<Void> = svc.delete(principal, workspace, types).doOnError { error ->
        logger.error("Error deleting types for workspace {}: {}", workspace, error.message, error)
    }.then()

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