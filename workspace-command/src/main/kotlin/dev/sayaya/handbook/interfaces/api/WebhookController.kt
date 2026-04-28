package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Webhook
import dev.sayaya.handbook.usecase.WebhookService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 웹훅 등록/조회/삭제 REST 컨트롤러.
 *
 * **책임:** 워크스페이스 단위로 웹훅의 등록(POST), 목록 조회(GET), 삭제(DELETE) 요청을 처리한다.
 * 커스텀 미디어 타입 `application/vnd.sayaya.handbook.v1+json`을 사용한다.
 *
 * **의존관계:**
 * - [WebhookService] — 웹훅 비즈니스 로직 (등록/조회/삭제)
 *
 * **주의:** POST 요청 시 id는 서버에서 생성하므로 클라이언트가 제공하지 않아도 된다.
 * RegisterWebhookRequest를 통해 필요한 필드만 받는다.
 */
@RestController
@RequestMapping("/workspaces/{workspace}/webhooks")
class WebhookController(private val webhookService: WebhookService) {

    @PostMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @PathVariable workspace: UUID,
        @RequestBody request: RegisterWebhookRequest,
    ): Mono<Webhook> {
        val webhook = Webhook(
            id = UUID.randomUUID(),
            workspace = workspace,
            url = request.url,
            events = request.events,
        )
        return webhookService.register(webhook)
    }

    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun list(@PathVariable workspace: UUID): Flux<Webhook> {
        return webhookService.list(workspace)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable workspace: UUID,
        @PathVariable id: UUID,
    ): Mono<Void> = webhookService.delete(id)

    /**
     * 웹훅 등록 요청 DTO.
     *
     * @property url 콜백 URL
     * @property events 구독할 이벤트 타입 목록 (빈 목록 = 전체 이벤트)
     */
    data class RegisterWebhookRequest(
        val url: String,
        val events: List<String> = emptyList(),
    )
}
