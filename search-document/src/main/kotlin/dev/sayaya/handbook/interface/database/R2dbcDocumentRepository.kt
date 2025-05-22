package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Repository
class R2dbcDocumentRepository(private val template: R2dbcEntityTemplate): DocumentRepository {
    @Transactional(readOnly = true)
    override fun search(param: Search): Mono<Page<Document>> {
        val pageNumber = param.page
        val pageSize = param.limit
        val sort = Sort.by(Sort.Order.asc("serial"))
        val pageable = PageRequest.of(pageNumber, pageSize, sort)
        return PageImpl(listOf(
            Document(
                id=UUID.randomUUID(),
                type="1",
                serial=UUID.randomUUID().toString(),
                effectDateTime = Instant.now(),
                expireDateTime = Instant.now().plus(10, ChronoUnit.SECONDS),
                data = mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            ), Document(
                id=UUID.randomUUID(),
                type="1",
                serial=UUID.randomUUID().toString(),
                effectDateTime = Instant.now(),
                expireDateTime = Instant.now().plus(10, ChronoUnit.SECONDS),
                data = mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            ), Document(
                id=UUID.randomUUID(),
                type="1",
                serial=UUID.randomUUID().toString(),
                effectDateTime = Instant.now(),
                expireDateTime = Instant.now().plus(10, ChronoUnit.SECONDS),
                data = mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            ), Document(
                id=UUID.randomUUID(),
                type="1",
                serial=UUID.randomUUID().toString(),
                effectDateTime = Instant.now(),
                expireDateTime = Instant.now().plus(10, ChronoUnit.SECONDS),
                data = mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            ), Document(
                id=UUID.randomUUID(),
                type="1",
                serial=UUID.randomUUID().toString(),
                effectDateTime = Instant.now(),
                expireDateTime = Instant.now().plus(10, ChronoUnit.SECONDS),
                data = mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            ), Document(
                id=UUID.randomUUID(),
                type="1",
                serial=UUID.randomUUID().toString(),
                effectDateTime = Instant.now(),
                expireDateTime = Instant.now().plus(10, ChronoUnit.SECONDS),
                data = mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            )
        ), pageable, 10).toMono()
    }
}