package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypeWithLayout
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@Service
class TypeService(
    private val repo: TypeRepository,
    private val layoutRepo: TypeLayoutRepository,
    private val layoutFactory: LayoutFactory,
    private val eventHandler: ExternalServiceHandler,
) {
    /*
     * 저장 Case
     * 1. 새로운 타입 생성            -> 새로운 레이아웃 생성
     * 2. 기존 타입 수정(버전업)       -> 새로운 레이아웃 생성
     * 3. 기존 타입 수정(버전 동일)    -> 기존 레이아웃 수정
     *
     * 저장 프로세스
     * 1. 타입 정보를 저장한다.
     * 2. 전체 Batch의 ID와 버전을 사용하여 레이아웃 UUID를 생성한다.
     * 3. 레이아웃을 조회하고, 레이아웃이 존재하지 않으면 새로운 레이아웃을 생성한다.
     * 4. 타입 레이아웃 정보를 저장한다.
     */
    @Transactional
    fun save(principal: Principal, workspace: UUID, typeWithLayouts: List<TypeWithLayout>): Mono<List<TypeWithLayout>> =
        if (typeWithLayouts.isEmpty()) Mono.empty()
        else typeWithLayouts.toType()
        .let { types -> repo.saveAll(workspace, types) }
        .flatMap { savedTypes -> layoutFactory.getOrCreateLayouts(workspace, savedTypes) }
        .flatMap { layoutRepo.saveAll(it, typeWithLayouts) }
        .delayUntil { eventHandler.publish(principal, workspace, it) }

    private fun List<TypeWithLayout>.toType(): List<Type> = this.map { it.type }
}