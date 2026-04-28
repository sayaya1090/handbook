package dev.sayaya.handbook.interfaces.schedule

import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID

/**
 * WebClient를 사용하여 활성 워크스페이스 목록을 조회하는 어댑터.
 *
 * **책임:** workspace-query 또는 workspace-command 서비스에서
 * 활성 워크스페이스 ID 목록을 HTTP로 조회한다.
 *
 * **의존관계:**
 * - [WebClient] -- 워크스페이스 서비스 HTTP 클라이언트
 *
 * **주의:** 워크스페이스 서비스가 응답하지 않으면 빈 목록을 반환하여
 * 스케줄 감시가 중단되지 않도록 한다.
 */
class WebClientWorkspaceProvider(
    private val webClient: WebClient,
) : WorkspaceProvider {
    override fun getActiveWorkspaces(): List<UUID> {
        return try {
            webClient.get()
                .uri("/workspaces/active")
                .retrieve()
                .bodyToFlux(UUID::class.java)
                .collectList()
                .block() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
