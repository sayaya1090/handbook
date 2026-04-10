package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.usecase.AgentActivityEntry
import dev.sayaya.handbook.usecase.DistributionEntry
import dev.sayaya.handbook.usecase.QualityIssueEntry
import dev.sayaya.handbook.usecase.StatsService
import dev.sayaya.handbook.usecase.TimelineEntry
import dev.sayaya.handbook.usecase.WorkspaceSummary
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 대시보드 통계 REST 컨트롤러 (읽기 전용 CQRS).
 *
 * **책임:** 워크스페이스의 문서 타임라인 통계 및 타입별 분포 데이터를 REST API로 제공한다.
 *
 * **의존관계:**
 * - [StatsService] — 통계 비즈니스 로직
 *
 * **엔드포인트:**
 * - `GET /workspace/{workspace}/stats` — 워크스페이스 집계 통계
 * - `GET /workspace/{workspace}/stats/timeline?from=&to=&interval=` — 시간별 문서 통계
 * - `GET /workspace/{workspace}/stats/distribution` — 타입별 문서 분포
 * - `GET /workspace/{workspace}/quality-issues` — 품질 이슈 목록
 * - `GET /workspace/{workspace}/agent-activity` — 에이전트 활동 목록
 */
@RestController
class StatsController(private val svc: StatsService) {

    /**
     * 워크스페이스의 집계 통계(타입 수, 문서 수, 사용자 수)를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     */
    @GetMapping("/workspace/{workspace}/stats", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun summary(@PathVariable workspace: UUID): Mono<WorkspaceSummary> = svc.summary(workspace)

    /**
     * 워크스페이스의 품질 이슈 목록을 반환한다.
     *
     * @param workspace 워크스페이스 ID
     */
    @GetMapping("/workspace/{workspace}/quality-issues", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun qualityIssues(@PathVariable workspace: UUID): Flux<QualityIssueEntry> = svc.qualityIssues(workspace)

    /**
     * 워크스페이스의 최근 에이전트 활동 목록을 반환한다.
     *
     * @param workspace 워크스페이스 ID
     */
    @GetMapping("/workspace/{workspace}/agent-activity", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun agentActivity(@PathVariable workspace: UUID): Flux<AgentActivityEntry> = svc.agentActivity(workspace)

    /**
     * 지정 기간의 문서 타임라인 통계를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @param from 조회 시작 시각 (ISO-8601)
     * @param to 조회 종료 시각 (ISO-8601)
     * @param interval 그룹화 간격 (일 단위, 기본 1)
     */
    @GetMapping("/workspace/{workspace}/stats/timeline", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun timeline(
        @PathVariable workspace: UUID,
        @RequestParam from: String,
        @RequestParam to: String,
        @RequestParam(required = false, defaultValue = "1") interval: Long,
    ): Flux<TimelineEntry> = svc.timeline(workspace, Instant.parse(from), Instant.parse(to), interval)

    /**
     * 워크스페이스의 타입별 문서 수 분포를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     */
    @GetMapping("/workspace/{workspace}/stats/distribution", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun distribution(
        @PathVariable workspace: UUID,
    ): Flux<DistributionEntry> = svc.distribution(workspace)
}
