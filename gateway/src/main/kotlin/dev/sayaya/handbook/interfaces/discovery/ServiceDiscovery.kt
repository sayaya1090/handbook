package dev.sayaya.handbook.interfaces.discovery

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.usecase.MenuSupplier
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Duration

/**
 * WebClient를 통해 개별 서비스로부터 메뉴를 조회하는 어댑터.
 *
 * **책임:** [MenuSupplier] 포트를 구현하여, 지정된 서비스의 `/menus` 엔드포인트로
 * HTTP GET 요청을 보내고 [Menu] 리스트를 반환한다.
 *
 * **의존관계:**
 * - [WebClient] — HTTP 통신 (서비스 이름 기반 base URL)
 *
 * **주의:** 타임아웃은 1200ms로, 응답 지연 시 빈 결과로 대체된다 (graceful degradation은 [MenuService] 참조).
 *
 * @param service 서비스 이름 (Kubernetes 서비스 DNS 등)
 */
class ServiceDiscovery(
    clientBuilder: WebClient.Builder,
    service: String,
) : MenuSupplier {
    private val client = clientBuilder.baseUrl("http://$service").build()

    override fun menu(headers: Map<String, List<String>>): Flux<Menu> = client.get()
        .uri(MENU_URI)
        .headers { h -> headers.forEach { (k, v) -> h.addAll(k, v) } }
        .accept(MediaType.parseMediaType(ACCEPT_MEDIA_TYPE))
        .retrieve()
        .bodyToFlux(Menu::class.java)
        .timeout(Duration.ofMillis(1200))

    companion object {
        const val MENU_URI = "/menus"
        const val ACCEPT_MEDIA_TYPE = "application/vnd.sayaya.handbook.v1+json"
    }
}
