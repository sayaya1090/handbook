package dev.sayaya.handbook.`interface`.api

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

    @PostMapping(value = ["/workspace/{workspace}/types"])
    @Transactional
    fun save(@AuthenticationPrincipal principal: Principal, @PathVariable workspace: UUID, @RequestBody types: List<TypeParam>): Mono<Void> {
        if (types.isEmpty()) return Mono.empty()
        val (toDeletes, toUpserts) = types.partition { it.delete }
        val update = if (toUpserts.isNotEmpty()) svc.save(principal, workspace, toUpserts.map(TypeParam::type)) else Mono.empty()
        val delete = if (toDeletes.isNotEmpty()) svc.delete(principal, workspace, toDeletes.map(TypeParam::type)) else Mono.empty()
        return update.mergeWith(delete).doOnError { error ->
            logger.error("Error saving or deleting types for workspace {}: {}", workspace, error.message, error)
        }.then()
    }
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