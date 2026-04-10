package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import reactor.core.publisher.Flux
import java.util.*

interface LayoutRepository {
    fun findByWorkspace(workspace: UUID): Flux<TypeLayout>
}
