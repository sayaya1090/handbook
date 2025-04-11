package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.client.domain.Menu
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Service
class MenuService(private val suppliers: List<MenuSupplier>) {
    fun menus(headers: HttpHeaders): Flux<Menu> = Flux.fromIterable(suppliers)
        .parallel().runOn(Schedulers.parallel())
        .flatMap { it.menu(headers).onErrorResume { Flux.empty() } }
        .sequential().sort(compareBy(nullsLast()){ it.order() })
}