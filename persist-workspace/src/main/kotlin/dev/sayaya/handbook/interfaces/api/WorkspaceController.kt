package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

/**
 * 워크스페이스 CUD REST 컨트롤러.
 *
 * **역할:** 워크스페이스 생성/수정/삭제/참여 HTTP 엔드포인트 제공.
 *
 * **책임:** 요청 파라미터 검증(이름 정규식, 길이 제한) 후 [WorkspaceService]에 위임한다.
 * 검증 실패 시 400 Bad Request를 반환한다.
 *
 * **의존관계:**
 * - [WorkspaceService] -- 워크스페이스 도메인 로직
 *
 * **주의:** 프론트엔드(SubmitButton)에도 동일한 정규식 검증이 있다.
 * 보안 목적의 최종 검증은 이 컨트롤러에서 수행한다.
 */
@RestController
@RequestMapping("/workspace")
class WorkspaceController(private val svc: WorkspaceService) {

    companion object {
        /** 워크스페이스 이름 검증: 영문/한글/숫자/하이픈/언더스코어, 1~255자 */
        private val NAME_PATTERN = Regex("^[a-zA-Z0-9가-힣\\-_]{1,255}$")
    }

    @PostMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun create(
        @AuthenticationPrincipal principal: Principal,
        @RequestBody param: CreateWorkspaceRequest,
    ): Mono<Workspace> {
        validateName(param.name)
        return svc.create(principal, param.name, param.description)
    }

    @PutMapping(
        value = ["/{id}"],
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun update(
        @PathVariable id: UUID,
        @RequestBody param: UpdateWorkspaceRequest,
    ): Mono<Workspace> {
        validateName(param.name)
        return svc.update(Workspace(id, param.name, param.description))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID): Mono<Void> = svc.delete(id)

    /**
     * 기존 워크스페이스에 참여를 요청한다.
     *
     * @param id 참여할 워크스페이스 ID
     * @param principal 참여 요청자
     * @return 완료 시그널 (204 No Content)
     */
    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun join(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: Principal?,
    ): Mono<Void> = svc.join(id, principal)

    private fun validateName(name: String) {
        if (!NAME_PATTERN.matches(name)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Workspace name must contain only alphanumeric, Korean, hyphen, or underscore characters (max 255)"
            )
        }
    }

    data class CreateWorkspaceRequest(val name: String, val description: String? = null)
    data class UpdateWorkspaceRequest(val name: String, val description: String? = null)
}
