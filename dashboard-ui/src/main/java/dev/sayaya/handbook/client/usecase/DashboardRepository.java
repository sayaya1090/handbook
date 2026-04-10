package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.AgentActivity;
import dev.sayaya.handbook.client.domain.QualityIssue;
import dev.sayaya.handbook.client.domain.WorkspaceStats;
import dev.sayaya.rx.Observable;

/** 대시보드 데이터 조회 포트 인터페이스. */
public interface DashboardRepository {
    Observable<WorkspaceStats> fetchStats();
    Observable<QualityIssue[]> fetchQualityIssues();
    Observable<AgentActivity[]> fetchAgentActivity();
}
