package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Layout
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.Comparator.comparing
import java.util.UUID

@Service
class LayoutService(private val repo: LayoutRepository) {
    fun findAll(workspace: UUID): Flux<Layout> = repo.findAll(workspace).sort(comparing(Layout::effectDateTime))
}