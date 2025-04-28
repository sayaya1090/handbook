package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.TypeWithLayout
import reactor.core.publisher.Mono

interface TypeLayoutRepository {
    fun saveAll(layout: Layout, typeWithLayout: List<TypeWithLayout>): Mono<List<TypeWithLayout>>
}