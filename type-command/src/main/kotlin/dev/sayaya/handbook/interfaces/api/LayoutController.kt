package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.TypeLayout
import dev.sayaya.handbook.usecase.LayoutService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 타입 레이아웃 REST 컨트롤러.
 *
 * **책임:** 워크스페이스 단위로 타입 캔버스 레이아웃(노드 위치)의 조회(GET)와 저장(PUT) 요청을 처리한다.
 * 커스텀 미디어 타입 `application/vnd.sayaya.handbook.v1+json`을 사용한다.
 *
 * **의존관계:**
 * - [LayoutService] — 레이아웃 비즈니스 로직
 */
@RestController
@RequestMapping("/workspaces/{workspace}/layouts")
class LayoutController(private val layoutService: LayoutService) {

    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun findByWorkspace(@PathVariable workspace: UUID): Flux<TypeLayout> =
        layoutService.findByWorkspace(workspace)

    @PutMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun save(
        @PathVariable workspace: UUID,
        @RequestBody layout: TypeLayout,
    ): Mono<TypeLayout> = layoutService.save(workspace, layout)
}
