package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.DiffResult
import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 타입 읽기 전용 비즈니스 로직 (CQRS query-side).
 *
 * **책임:** 기간별 타입 조회 및 두 버전 간 diff 계산.
 * type-command 모듈의 CUD 서비스와 네임스페이스 충돌을 방지하기 위해 "Search" 접두사를 사용한다.
 *
 * **의존관계:**
 * - [TypeSearchRepository] — R2DBC 조회 포트
 */
class TypeSearchService(private val repo: TypeSearchRepository) {
    fun findByRange(workspace: UUID, effectDateTime: Instant?, expireDateTime: Instant?): Flux<Type> =
        if (effectDateTime != null) repo.findByRange(workspace, effectDateTime, expireDateTime ?: effectDateTime)
        else repo.findAll(workspace)

    /**
     * 특정 타입의 모든 버전을 조회한다.
     *
     * @param workspace 워크스페이스 ID
     * @param typeId 타입 ID
     * @return 해당 타입의 모든 버전 Flux
     */
    fun findVersions(workspace: UUID, typeId: String): Flux<Type> = repo.findVersions(workspace, typeId)

    /**
     * 타입의 두 버전 간 diff를 계산한다.
     * 속성 추가/삭제/변경, description, parent 변경을 감지한다.
     */
    fun diff(workspace: UUID, typeId: String, v1: String, v2: String): Mono<DiffResult> {
        return Mono.zip(
            repo.findByIdAndVersion(workspace, typeId, v1),
            repo.findByIdAndVersion(workspace, typeId, v2),
        ).map { tuple ->
            val old = tuple.t1
            val new = tuple.t2
            val changes = mutableListOf<String>()
            val added = mutableListOf<String>()
            val removed = mutableListOf<String>()

            if (old.description != new.description)
                changes.add("description: ${old.description ?: "(없음)"} → ${new.description ?: "(없음)"}")
            if (old.parent != new.parent)
                changes.add("parent: ${old.parent ?: "(없음)"} → ${new.parent ?: "(없음)"}")

            val oldAttrs = old.attributes.associateBy { it.name }
            val newAttrs = new.attributes.associateBy { it.name }

            (newAttrs.keys - oldAttrs.keys).forEach { added.add(it) }
            (oldAttrs.keys - newAttrs.keys).forEach { removed.add(it) }

            oldAttrs.keys.intersect(newAttrs.keys).forEach { name ->
                val a = oldAttrs[name]!!
                val b = newAttrs[name]!!
                if (a.type != b.type) changes.add("$name [type]: ${a.type} → ${b.type}")
                if (a.nullable != b.nullable) changes.add("$name [nullable]: ${a.nullable} → ${b.nullable}")
                if (a.order != b.order) changes.add("$name [order]: ${a.order} → ${b.order}")
            }

            DiffResult(changes, added, removed)
        }
    }
}
