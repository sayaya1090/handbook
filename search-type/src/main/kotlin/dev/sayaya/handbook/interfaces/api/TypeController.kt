package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.DiffResult
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeSearchService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 타입 조회 REST 컨트롤러 (읽기 전용 CQRS).
 *
 * **책임:** 기간별 타입 목록 조회 및 두 버전 간 diff 제공.
 *
 * **의존관계:**
 * - [TypeSearchService] — 조회/diff 비즈니스 로직
 */
@RestController
@RequestMapping("/workspace/{workspace}/types")
class TypeController(private val svc: TypeSearchService) {

    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun find(
        @PathVariable workspace: UUID,
        @RequestParam("effect_date_time", required = false) effectDateTime: Instant?,
        @RequestParam("expire_date_time", required = false) expireDateTime: Instant?,
    ): Flux<Type> = svc.findByRange(workspace, effectDateTime, expireDateTime)

    /**
     * 특정 타입의 모든 버전을 조회한다.
     *
     * @param workspace 워크스페이스 ID
     * @param typeId 타입 ID
     * @return 해당 타입의 모든 버전 목록
     */
    @GetMapping("/{typeId}/versions", produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun versions(
        @PathVariable workspace: UUID,
        @PathVariable typeId: String,
    ): Flux<Type> = svc.findVersions(workspace, typeId)

    @GetMapping("/{typeId}/diff", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun diff(
        @PathVariable workspace: UUID,
        @PathVariable typeId: String,
        @RequestParam v1: String,
        @RequestParam v2: String,
    ): Mono<DiffResult> = svc.diff(workspace, typeId, v1, v2)
}
