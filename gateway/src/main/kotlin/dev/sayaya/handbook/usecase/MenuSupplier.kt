package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Menu
import reactor.core.publisher.Flux

/**
 * 메뉴를 제공하는 외부 서비스에 대한 포트(인터페이스).
 * interfaces 계층에서 구체적인 서비스 디스커버리 방식으로 구현한다.
 */
interface MenuSupplier {
    fun menu(headers: Map<String, List<String>>): Flux<Menu>
}
