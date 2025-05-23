package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentService
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
class DocumentController(private val svc: DocumentService) {
    private val logger = LoggerFactory.getLogger(DocumentController::class.java)

    @PostMapping(value = ["/workspace/{workspace}/documents"])
    @Transactional
    fun save(@AuthenticationPrincipal principal: Principal, @PathVariable workspace: UUID, @RequestBody documents: List<DocumentParam>): Mono<Void> {
        if (documents.isEmpty()) return Mono.empty()
        val (toDeletes, toUpserts) = documents.partition { it.delete }
        val update = if (toUpserts.isNotEmpty()) svc.save(principal, workspace, toUpserts.map(DocumentParam::document)) else Mono.empty()
        val delete: Mono<List<Document>> = Mono.empty()
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