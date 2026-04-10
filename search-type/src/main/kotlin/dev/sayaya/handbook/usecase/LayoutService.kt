package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import reactor.core.publisher.Flux
import java.util.*

class LayoutService(private val repo: LayoutRepository) {
    fun findByWorkspace(workspace: UUID): Flux<TypeLayout> = repo.findByWorkspace(workspace)
}
