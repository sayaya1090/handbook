package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.TypeLayout
import reactor.core.publisher.Flux
import java.util.*

/**
 * 레이아웃 읽기 전용 조회 포트 (CQRS query-side).
 *
 * **책임:** 워크스페이스별 타입 레이아웃 조회를 정의한다.
 * persist 모듈과의 네임스페이스 충돌을 방지하기 위해 "Search" 접두사를 사용한다.
 */
interface LayoutSearchRepository {
    fun findByWorkspace(workspace: UUID): Flux<TypeLayout>
}
