package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.SchemaPatch
import dev.sayaya.handbook.usecase.SchemaService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * 스키마 일괄 패치 REST 컨트롤러.
 * 
 * **책임:** 워크스페이스 단위로 타입 및 레이아웃의 원자적 일괄 변경(생성/수정/삭제) 요청을 처리한다.
 */
@RestController
@RequestMapping("/workspaces/{workspace}/schema")
class SchemaController(private val schemaService: SchemaService) {

    @PatchMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun patch(
        @PathVariable workspace: UUID,
        @RequestBody patch: SchemaPatch,
    ): Mono<Void> = schemaService.patch(workspace, patch)
}
