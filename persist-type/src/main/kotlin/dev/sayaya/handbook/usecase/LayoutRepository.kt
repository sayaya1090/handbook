package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/** 타입 레이아웃 영속화 포트. */
interface LayoutRepository {
    fun findByWorkspace(workspace: UUID): Flux<TypeLayout>
    fun save(workspace: UUID, layout: TypeLayout): Mono<TypeLayout>
}
