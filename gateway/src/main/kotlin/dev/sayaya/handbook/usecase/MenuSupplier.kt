package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.client.domain.Menu
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Flux

interface MenuSupplier {
    fun menu(headers: HttpHeaders): Flux<Menu>
}