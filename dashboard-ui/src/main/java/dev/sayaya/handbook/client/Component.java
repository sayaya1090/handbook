package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.api.DashboardApi;
import dev.sayaya.handbook.client.interfaces.ui.DashboardElement;
import dev.sayaya.handbook.client.usecase.*;

import javax.inject.Singleton;

/**
 * 대시보드 모듈의 Dagger 루트 컴포넌트.
 *
 * <p><b>책임:</b> DashboardModule과 ApiModule을 조합하여 대시보드 UI 및 유스케이스 인스턴스를 생성한다.
 * ExecutionStatusList, ArtifactList를 통해 에이전트 실행 상태 및 아티팩트 데이터를 제공한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link DashboardModule} — 다국어, 언어팩 바인딩</li>
 *   <li>{@link ApiModule} — FetchApi, DashboardRepository 바인딩</li>
 * </ul></p>
 */
@Singleton
@dagger.Component(modules = { DashboardModule.class, ApiModule.class })
public interface Component {
    DashboardElement dashboard();
    StatsProvider statsProvider();
    QualityIssueList qualityIssueList();
    AgentActivityList agentActivityList();
    TimelineProvider timelineProvider();
    DistributionProvider distributionProvider();
    DashboardRepository dashboardRepository();
    DashboardApi dashboardApi();
    ExecutionStatusList executionStatusList();
    ArtifactList artifactList();
}
