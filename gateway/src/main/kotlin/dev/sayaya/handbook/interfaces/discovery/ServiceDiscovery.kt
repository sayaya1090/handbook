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
