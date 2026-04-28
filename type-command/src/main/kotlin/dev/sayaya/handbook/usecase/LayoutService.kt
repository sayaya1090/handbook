package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 타입 레이아웃 비즈니스 로직.
 *
 * **책임:** 워크스페이스별 타입 캔버스 레이아웃의 조회와 저장을 처리한다.
 * Spring 어노테이션 없이 POJO로 구현되며, [TypeConfig][dev.sayaya.handbook.interfaces.config.TypeConfig]에서 Bean으로 등록된다.
 *
 * **의존관계:**
 * - [LayoutRepository] — 레이아웃 영속화 포트
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
