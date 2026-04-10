package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 타입 레이아웃 비즈니스 로직.
 * Spring 어노테이션 없음 — interfaces.config에서 Bean 등록.
 */
class LayoutService(
    private val layoutRepository: LayoutRepository,
) {
    fun findByWorkspace(workspace: UUID): Flux<TypeLayout> {
        return layoutRepository.findByWorkspace(workspace)
    }

    fun save(workspace: UUID, layout: TypeLayout): Mono<TypeLayout> {
        return layoutRepository.save(workspace, layout)
    }
}
