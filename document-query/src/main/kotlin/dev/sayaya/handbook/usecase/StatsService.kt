package dev.sayaya.handbook.usecase

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 대시보드 통계 비즈니스 로직 (읽기 전용 CQRS).
 *
 * **책임:** 워크스페이스의 문서 타임라인 통계 및 타입별 분포를 조회한다.
 *
 * **의존관계:**
 * - [StatsRepository] — R2DBC 통계 조회 포트
 */
class StatsService(private val repo: StatsRepository) {
    /**
     * 지정 기간의 문서 타임라인 통계를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @param from 조회 시작 시각
     * @param to 조회 종료 시각
     * @param intervalDays 그룹화 간격 (일 단위, 기본 1)
     */
    fun timeline(workspace: UUID, from: Instant, to: Instant, intervalDays: Long = 1): Flux<TimelineEntry> =
        repo.timeline(workspace, from, to, intervalDays)

    /**
     * 워크스페이스의 타입별 문서 수 분포를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     */
    fun distribution(workspace: UUID): Flux<DistributionEntry> =
        repo.distribution(workspace)

    /**
     * 워크스페이스의 집계 통계(타입 수, 문서 수, 사용자 수)를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     */
    fun summary(workspace: UUID): Mono<WorkspaceSummary> =
        repo.summary(workspace)

    /**
     * 워크스페이스의 품질 이슈 목록을 반환한다.
     *
     * @param workspace 워크스페이스 ID
     */
    fun qualityIssues(workspace: UUID): Flux<QualityIssueEntry> =
        repo.qualityIssues(workspace)

    /**
     * 워크스페이스의 최근 에이전트 활동 목록을 반환한다.
     *
     * @param workspace 워크스페이스 ID
     */
    fun agentActivity(workspace: UUID): Flux<AgentActivityEntry> =
        repo.agentActivity(workspace)
}
