package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/** 문서 영속화 포트. */
interface DocumentRepository {
    fun saveAll(workspace: UUID, documents: List<Document>): Flux<Document>
    fun patchAll(workspace: UUID, patches: List<DocumentPatch>): Flux<Document>
    fun deleteAll(workspace: UUID, documents: List<Document>): Mono<Void>
}
