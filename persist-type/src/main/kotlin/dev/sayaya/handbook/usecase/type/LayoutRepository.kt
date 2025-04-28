package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.TypeWithLayout
import reactor.core.publisher.Mono
import java.util.*

interface LayoutRepository {
    fun findById(workspace: UUID, id: UUID): Mono<Layout>
    fun save(layout: Layout): Mono<Layout>
    fun save(layout: Layout, typeWithLayout: List<TypeWithLayout>): Mono<List<TypeWithLayout>>
}