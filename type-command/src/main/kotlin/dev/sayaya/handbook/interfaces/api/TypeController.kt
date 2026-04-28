package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypePatch
import dev.sayaya.handbook.usecase.TypeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 타입 CUD REST 컨트롤러.
 *
 * **책임:** 워크스페이스 단위로 타입의 기간별 조회(GET), 저장(PUT), 부분 수정(PATCH), 삭제(DELETE) 요청을 처리한다.
 * 커스텀 미디어 타입 `application/vnd.sayaya.handbook.v1+json`을 사용한다.
 *
 * **의존관계:**
 * - [TypeService] — 타입 비즈니스 로직 (조회/저장/패치/삭제 + 이벤트 발행)
 *
 * **주의:** PATCH 시 rev 불일치가 발생하면 409 Conflict를 반환한다.
 * GET은 effect_date_time/expire_date_time 쿼리 파라미터로 기간 필터링한다.
 */
@RestController
@RequestMapping("/workspaces/{workspace}/types")
class TypeController(private val typeService: TypeService) {

    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun findByPeriod(
        @PathVariable workspace: UUID,
        @RequestParam("effect_date_time") effectDateTime: Instant,
        @RequestParam("expire_date_time") expireDateTime: Instant,
    ): Flux<Type> = typeService.findByPeriod(workspace, effectDateTime, expireDateTime)

    @PutMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun save(
        @PathVariable workspace: UUID,
        @RequestBody types: List<Type>,
    ): Flux<Type> = typeService.save(workspace, types)

    @PatchMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun patch(
        @PathVariable workspace: UUID,
        @RequestBody patches: List<TypePatch>,
    ): Flux<Type> = typeService.patch(workspace, patches)

    @DeleteMapping(consumes = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable workspace: UUID,
        @RequestBody types: List<Type>,
    ): Mono<Void> = typeService.delete(workspace, types)
}
