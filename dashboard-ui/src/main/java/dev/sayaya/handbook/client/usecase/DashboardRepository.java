package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.*;
import dev.sayaya.rx.Observable;

/**
 * 대시보드 데이터 조회 포트 인터페이스.
 *
 * <p><b>책임:</b> 워크스페이스 통계, 품질 이슈, 에이전트 활동, 타임라인, 분포 데이터를 조회하는 유스케이스 포트를 정의한다.</p>
 * <p><b>의존관계:</b> <ul><li>interfaces 계층의 {@link dev.sayaya.handbook.client.interfaces.api.DashboardApi}가 구현한다.</li></ul></p>
 */
public interface DashboardRepository {
    Observable<WorkspaceStats> fetchStats();
    Observable<QualityIssue[]> fetchQualityIssues();
    Observable<AgentActivity[]> fetchAgentActivity();

    /**
     * 지정 기간의 문서 타임라인 통계를 조회한다.
     *
     * @param from 조회 시작 시각 (ISO-8601)
     * @param to 조회 종료 시각 (ISO-8601)
     * @param interval 그룹화 간격 (일 단위)
     * @return 타임라인 데이터 배열의 Observable
     */
    Observable<TimelineData[]> timeline(String from, String to, int interval);

    /**
     * 워크스페이스의 타입별 문서 수 분포를 조회한다.
     *
     * @return 분포 데이터 배열의 Observable
     */
    Observable<DistributionData[]> distribution();

    /**
     * 현재 워크스페이스의 활성 에이전트 실행 목록을 조회한다.
     *
     * @return 실행 상태 데이터 배열의 Observable
     */
    Observable<ExecutionStatusData[]> executions();

    /**
     * 현재 워크스페이스의 완료된 아티팩트 목록을 조회한다.
     *
     * @return 아티팩트 데이터 배열의 Observable
     */
    Observable<ArtifactData[]> artifacts();
}
