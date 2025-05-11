package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*
import java.util.Comparator.comparing

@Service
class TypeService(private val repo: TypeRepository) {
    fun findAll(workspace: UUID): Flux<Layout> = repo.findAll(workspace).sort(comparing(Layout::effectDateTime))
    fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<Type> = repo.findByRange(workspace, effectDateTime, expireDateTime)
}