package dev.sayaya.handbook.`interface`.cache

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface TypeRangeCache {
    // 특정 워크스페이스 내에서 주어진 시간 범위에 해당하는 Type 목록을 조회
    fun findTypesByTimeRange(workspace: UUID, id: String, startInstant: Instant, endInstant: Instant): Mono<Type>

    // 단일 Type 객체를 캐시에 저장 또는 업데이트
    fun saveType(workspace: UUID, type: Type): Mono<Void>

    // 특정 워크스페이스의 모든 Type 캐시를 비움
    fun clearCache(workspace: UUID, id: String): Mono<Void>
}
