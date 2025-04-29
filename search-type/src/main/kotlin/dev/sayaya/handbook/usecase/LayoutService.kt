package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeWithLayout
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Service
class LayoutService(private val repo: LayoutRepository) {
    fun search(workspace: UUID, baseTime: Instant): Flux<TypeWithLayout> = repo.findByBaseTime(workspace, baseTime)
}