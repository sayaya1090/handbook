package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeWithLayout
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

interface LayoutRepository {
    fun findByBaseTime(workspace: UUID, baseTime: Instant): Flux<TypeWithLayout>
}