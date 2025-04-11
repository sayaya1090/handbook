package dev.sayaya.handbook.`interface`.k8s

import dev.sayaya.handbook.client.domain.Menu
import dev.sayaya.handbook.usecase.MenuSupplier
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Duration

class ServiceDiscovery (
    private val client: WebClient.Builder,
    private val service: String
): MenuSupplier {
    override fun menu(headers: HttpHeaders): Flux<Menu> = client.baseUrl("http://$service").build().get()
        .uri("/menus")
        .headers{h->headers.forEach(h::addAll)}
        .accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
        .retrieve()
        .bodyToFlux(Menu::class.java)
        .timeout(Duration.ofMillis(1200))
}