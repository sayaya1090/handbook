package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.AgentActivity;
import dev.sayaya.handbook.client.domain.QualityIssue;
import dev.sayaya.handbook.client.domain.WorkspaceStats;
import dev.sayaya.rx.Observable;

/**
 * 대시보드 데이터 조회 포트 인터페이스.
 *
 * <p><b>책임:</b> 워크스페이스 통계, 품질 이슈, 에이전트 활동 데이터를 조회하는 유스케이스 포트를 정의한다.</p>
 * <p><b>의존관계:</b> <ul><li>interfaces 계층의 {@link dev.sayaya.handbook.client.interfaces.api.DashboardApi}가 구현한다.</li></ul></p>
 */
public interface DashboardRepository {
    Observable<WorkspaceStats> fetchStats();
    Observable<QualityIssue[]> fetchQualityIssues();
    Observable<AgentActivity[]> fetchAgentActivity();
}
