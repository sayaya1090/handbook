package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Layout
import reactor.core.publisher.Flux
import java.util.*

interface LayoutRepository {
    fun findAll(workspace: UUID): Flux<Layout>
}