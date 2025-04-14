package dev.sayaya.handbook.`interface`.k8s

import dev.sayaya.handbook.client.domain.Menu
import dev.sayaya.handbook.usecase.MenuSupplier
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Duration

class ServiceDiscovery (clientBuilder: WebClient.Builder, service: String): MenuSupplier {
    companion object {
        const val URI = "/menus"
        const val ACCEPT_MEDIA_TYPE = "application/vnd.sayaya.handbook.v1+json"
    }
    private val url: String = "http://$service"
    private val client = clientBuilder.baseUrl(url).build()
    override fun menu(headers: HttpHeaders): Flux<Menu> = client.get()
        .uri(URI)
        .headers{h->headers.forEach(h::addAll)}
        .accept(MediaType.parseMediaType(ACCEPT_MEDIA_TYPE))
        .retrieve()
        .bodyToFlux(Menu::class.java)
        .timeout(Duration.ofMillis(1200))
}