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
    fun findByBaseTime(workspace: UUID, baseTime: Instant): Flux<TypeWithLayout> = repo.findByBaseTime(workspace, baseTime)
}