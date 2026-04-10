package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Menu
import reactor.core.publisher.Flux

/**
 * 메뉴를 제공하는 외부 서비스에 대한 포트 (헥사고날 아키텍처 출력 포트).
 *
 * **책임:** 외부 서비스의 메뉴 조회 계약을 정의한다.
 * 요청 헤더(인증 정보 등)를 전달받아 서비스별 메뉴를 반환한다.
 *
 * **의존관계:**
 * - [ServiceDiscovery][dev.sayaya.handbook.interfaces.discovery.ServiceDiscovery] — WebClient 기반 구현체
 */
interface MenuSupplier {
    fun menu(headers: Map<String, List<String>>): Flux<Menu>
}
