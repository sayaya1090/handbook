package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Layout
import reactor.core.publisher.Mono
import java.util.*

interface LayoutRepository {
    fun findById(workspace: UUID, id: UUID): Mono<Layout>
    fun save(layout: Layout): Mono<Layout>
}