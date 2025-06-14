package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

interface ExternalService {
    fun update(principal: Principal, workspace: UUID, documents: List<Document>): Mono<Void>
    fun delete(principal: Principal, workspace: UUID, documents: List<Document>): Mono<Void>
}