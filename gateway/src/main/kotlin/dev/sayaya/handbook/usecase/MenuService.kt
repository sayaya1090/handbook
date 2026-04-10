package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Menu
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

/**
 * 여러 서비스로부터 메뉴를 병렬로 수집하고 정렬하여 반환하는 유스케이스.
 *
 * 개별 서비스 실패 시에도 다른 서비스의 결과는 정상 반환한다 (graceful degradation).
 */
class MenuService(private val suppliers: List<MenuSupplier>) {
    fun menus(headers: Map<String, List<String>>): Flux<Menu> = Flux.fromIterable(suppliers)
        .parallel().runOn(Schedulers.parallel())
        .flatMap { it.menu(headers).onErrorResume { Flux.empty() } }
        .sequential().sort(compareBy(nullsLast()) { it.order() })
}
