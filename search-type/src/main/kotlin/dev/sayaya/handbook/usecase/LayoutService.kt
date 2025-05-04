package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.TypeWithLayout
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*
import java.util.Comparator.comparing

@Service
class LayoutService(private val repo: LayoutRepository) {
    fun findAll(workspace: UUID): Flux<Layout> = repo.findAll(workspace).sort(comparing(Layout::effectDateTime))
    fun findByRange(workspace: UUID, effectDateTime: Instant, expireDateTime: Instant): Flux<TypeWithLayout> = repo.findByRange(workspace, effectDateTime, expireDateTime)
}