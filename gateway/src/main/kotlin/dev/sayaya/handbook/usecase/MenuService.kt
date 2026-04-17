package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Menu
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration

/**
 * 여러 서비스로부터 메뉴를 병렬로 수집하고 정렬하여 반환하는 유스케이스.
 *
 * **책임:** 등록된 [MenuSupplier] 목록을 parallel Scheduler에서 동시 호출하고,
 * order 기준으로 정렬하여 단일 스트림으로 합친다.
 *
 * **의존관계:**
 * - [MenuSupplier] — 개별 서비스 메뉴 조회 포트 (1:N)
 *
 * **주의:** 개별 서비스 실패 시 `onErrorResume { Flux.empty() }`로 무시하므로,
 * 일부 서비스 장애에도 다른 서비스의 결과는 정상 반환된다 (graceful degradation).
 */
class MenuService(private val suppliers: List<MenuSupplier>) {
    companion object {
        /**
         * 집계 전체 컷오프. 개별 [ServiceDiscovery] 타임아웃(500ms) 이
         * 걸리지 않는 엣지 케이스(소켓 레벨 hanging, sort 가 upstream complete 대기)
         * 에서도 /menus 응답이 막히지 않도록 2차 방어선.
         */
        private val AGGREGATE_TIMEOUT: Duration = Duration.ofSeconds(1)
    }

    fun menus(headers: Map<String, List<String>>): Flux<Menu> = Flux.fromIterable(suppliers)
        .parallel().runOn(Schedulers.parallel())
        .flatMap { it.menu(headers).onErrorResume { Flux.empty() } }
        .sequential().sort(compareBy(nullsLast()) { it.order() })
        .timeout(AGGREGATE_TIMEOUT)
        .onErrorResume { Flux.empty() }
}
