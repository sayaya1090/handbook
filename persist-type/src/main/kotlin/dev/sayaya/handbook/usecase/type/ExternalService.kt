package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.TypeWithLayout
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

interface ExternalService {
    fun publish(principal: Principal, workspace: UUID, typeWithLayouts: List<TypeWithLayout>): Mono<Void>
}