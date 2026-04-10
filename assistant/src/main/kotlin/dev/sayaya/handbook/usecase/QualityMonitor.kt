package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.QualityIssue
import reactor.core.publisher.Flux
import java.util.UUID

interface QualityMonitor {
    fun scan(workspace: UUID): Flux<QualityIssue>
}
