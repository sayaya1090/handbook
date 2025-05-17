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

    @PutMapping(value = ["/workspace/{workspace}/documents"])
    @Transactional
    fun save(@AuthenticationPrincipal principal: Principal, @PathVariable workspace: UUID, @RequestBody documents: List<Document>): Mono<Void> {
        if (documents.isEmpty()) return Mono.empty()
        return svc.save(principal, workspace, documents).doOnError { error ->
            logger.error("Error saving document for workspace {}: {}", workspace, error.message, error)
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