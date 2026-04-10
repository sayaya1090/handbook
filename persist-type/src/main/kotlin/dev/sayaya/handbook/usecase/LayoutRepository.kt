package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 타입 레이아웃 영속화 포트 (헥사고날 아키텍처 출력 포트).
 *
 * **책임:** 워크스페이스별 타입 캔버스 레이아웃(노드 위치)의 조회 및 저장을 정의한다.
 *
 * **의존관계:**
 * - [R2dbcLayoutRepositoryAdapter][dev.sayaya.handbook.interfaces.database.R2dbcLayoutRepositoryAdapter] — R2DBC 구현체
 */
interface LayoutRepository {
    fun findByWorkspace(workspace: UUID): Flux<TypeLayout>
    fun save(workspace: UUID, layout: TypeLayout): Mono<TypeLayout>
}
