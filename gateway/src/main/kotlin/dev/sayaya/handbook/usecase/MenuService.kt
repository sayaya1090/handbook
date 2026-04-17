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
         * 집계 전체 컷오프. `Flux.take(Duration)` 으로 적용하여 시간 경과 시
         * 빈 Flux 로 덮는 대신 **지금까지 수집된 부분 결과를 그대로 emit** 한다
         * (shell-ui MenuRail 이 빈 상태로 빠지는 silent degradation 방지).
         *
         * 개별 supplier 는 [ServiceDiscovery] 500ms 타임아웃으로 격리되고,
         * 이 값은 병렬 합산 + sort 단계의 최종 방어선이다.
         */
        private val AGGREGATE_TIMEOUT: Duration = Duration.ofMillis(1500)
    }

    fun menus(headers: Map<String, List<String>>): Flux<Menu> = Flux.fromIterable(suppliers)
        .parallel().runOn(Schedulers.parallel())
        .flatMap { it.menu(headers).onErrorResume { Flux.empty() } }
        .sequential()
        .take(AGGREGATE_TIMEOUT)
        .sort(compareBy(nullsLast()) { it.order() })
}
