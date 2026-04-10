package dev.sayaya.handbook.usecase

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * 통계 데이터 조회 포트 인터페이스 (읽기 전용 CQRS).
 *
 * **책임:** 워크스페이스의 문서 타임라인 통계 및 타입별 분포 데이터를 조회한다.
 *
 * **의존관계:**
 * - interfaces 계층의 R2DBC 구현체가 이 포트를 구현한다.
 */
interface StatsRepository {
    /**
     * 지정 기간 내 문서 통계를 시간 간격별로 그룹화하여 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @param from 조회 시작 시각
     * @param to 조회 종료 시각
     * @param intervalDays 그룹화 간격 (일 단위)
     * @return 날짜별 문서 수, 검증 실패 수, 에이전트 명령 수
     */
    fun timeline(workspace: UUID, from: Instant, to: Instant, intervalDays: Long): Flux<TimelineEntry>

    /**
     * 워크스페이스의 타입별 문서 수 분포를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @return 타입명과 해당 문서 수
     */
    fun distribution(workspace: UUID): Flux<DistributionEntry>

    /**
     * 워크스페이스의 집계 통계(타입 수, 문서 수, 사용자 수)를 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @return 집계 통계
     */
    fun summary(workspace: UUID): Mono<WorkspaceSummary>

    /**
     * 워크스페이스의 품질 이슈 목록을 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @return 품질 이슈 목록
     */
    fun qualityIssues(workspace: UUID): Flux<QualityIssueEntry>

    /**
     * 워크스페이스의 최근 에이전트 활동 목록을 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @return 에이전트 활동 목록
     */
    fun agentActivity(workspace: UUID): Flux<AgentActivityEntry>
}

/**
 * 타임라인 통계의 단일 시간 구간 데이터.
 *
 * @property date 구간의 시작 날짜 (ISO-8601 문자열)
 * @property documentCount 해당 구간의 문서 수
 * @property validationFailures 해당 구간의 검증 실패 수
 * @property agentCommands 해당 구간의 에이전트 명령 수
 */
data class TimelineEntry(
    val date: String,
    val documentCount: Long,
    val validationFailures: Long = 0,
    val agentCommands: Long = 0,
)

/**
 * 타입별 문서 분포 데이터.
 *
 * @property type 문서 타입명
 * @property count 해당 타입의 문서 수
 */
data class DistributionEntry(
    val type: String,
    val count: Long,
)

/**
 * 워크스페이스 집계 통계.
 *
 * @property typeCount 타입 수
 * @property documentCount 문서 수
 * @property userCount 사용자 수
 */
data class WorkspaceSummary(
    val typeCount: Long,
    val documentCount: Long,
    val userCount: Long,
)

/**
 * 품질 이슈 항목.
 *
 * @property documentId 문서 ID
 * @property serial 문서 시리얼
 * @property type 문서 타입
 * @property issue 이슈 설명
 * @property severity 심각도 (warning, error)
 */
data class QualityIssueEntry(
    val documentId: String,
    val serial: String,
    val type: String,
    val issue: String,
    val severity: String,
)

/**
 * 에이전트 활동 항목.
 *
 * @property agentId 에이전트 ID
 * @property action 수행한 동작
 * @property target 대상 (문서/타입 ID)
 * @property timestamp 활동 시각 (ISO-8601)
 * @property count 동작 수
 */
data class AgentActivityEntry(
    val agentId: String,
    val action: String,
    val target: String,
    val timestamp: String,
    val count: Long,
)
