package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.domain.AgentActivity;
import dev.sayaya.handbook.domain.ArtifactData;
import dev.sayaya.handbook.domain.DistributionData;
import dev.sayaya.handbook.domain.ExecutionStatusData;
import dev.sayaya.handbook.domain.QualityIssue;
import dev.sayaya.handbook.domain.TimelineData;
import dev.sayaya.handbook.domain.WorkspaceStats;
import dev.sayaya.handbook.client.usecase.DashboardRepository;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import dev.sayaya.handbook.usecase.FetchApi;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * DashboardRepository의 HTTP 구현체.
 *
 * <p><b>책임:</b> FetchApi를 사용하여 Gateway로부터 통계, 품질 이슈, 에이전트 활동 데이터를 조회한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link FetchApi} — HTTP 요청 전송</li>
 *   <li>{@link AsyncSubject} — Promise를 Observable로 변환</li>
 * </ul></p>
 * <p><b>주의:</b> 요청 실패 시 null 또는 빈 배열을 반환하며, GWT.log로 오류를 기록한다.
 * {@link #setWorkspace(String)}으로 워크스페이스 ID를 설정한 후 API를 호출해야 한다.</p>
 */
@Singleton
public class DashboardApi implements DashboardRepository {
    private final FetchApi fetchApi;
    private String workspace;

    @Inject
    public DashboardApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    /**
     * 대시보드 API 호출에 사용할 워크스페이스 ID를 설정한다.
     *
     * @param workspace 워크스페이스 ID
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public Observable<WorkspaceStats> fetchStats() {
        Promise<WorkspaceStats> promise = fetchApi.request("workspaces/" + workspace + "/stats")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<WorkspaceStats>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.fetchStats failed: " + err);
                    ErrorNotifier.notify("DashboardApi.fetchStats failed: " + err);
                    return Promise.resolve((WorkspaceStats) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<QualityIssue[]> fetchQualityIssues() {
        Promise<QualityIssue[]> promise = fetchApi.request("workspaces/" + workspace + "/quality-issues")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<QualityIssue[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.fetchQualityIssues failed: " + err);
                    ErrorNotifier.notify("DashboardApi.fetchQualityIssues failed: " + err);
                    return Promise.resolve(new QualityIssue[0]);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<AgentActivity[]> fetchAgentActivity() {
        Promise<AgentActivity[]> promise = fetchApi.request("workspaces/" + workspace + "/agent-activity")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<AgentActivity[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.fetchAgentActivity failed: " + err);
                    ErrorNotifier.notify("DashboardApi.fetchAgentActivity failed: " + err);
                    return Promise.resolve(new AgentActivity[0]);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<TimelineData[]> timeline(String from, String to, int interval) {
        String url = "workspaces/" + workspace + "/stats/timeline?from=" + from + "&to=" + to + "&interval=" + interval;
        Promise<TimelineData[]> promise = fetchApi.request(url)
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<TimelineData[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.timeline failed: " + err);
                    ErrorNotifier.notify("DashboardApi.timeline failed: " + err);
                    return Promise.resolve(new TimelineData[0]);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<DistributionData[]> distribution() {
        Promise<DistributionData[]> promise = fetchApi.request("workspaces/" + workspace + "/stats/distribution")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<DistributionData[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.distribution failed: " + err);
                    ErrorNotifier.notify("DashboardApi.distribution failed: " + err);
                    return Promise.resolve(new DistributionData[0]);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<ExecutionStatusData[]> executions() {
        Promise<ExecutionStatusData[]> promise = fetchApi.request("assistant/executions")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<ExecutionStatusData[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.executions failed: " + err);
                    ErrorNotifier.notify("DashboardApi.executions failed: " + err);
                    return Promise.resolve(new ExecutionStatusData[0]);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<ArtifactData[]> artifacts() {
        Promise<ArtifactData[]> promise = fetchApi.request("assistant/artifacts")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<ArtifactData[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.artifacts failed: " + err);
                    ErrorNotifier.notify("DashboardApi.artifacts failed: " + err);
                    return Promise.resolve(new ArtifactData[0]);
                });
        return AsyncSubject.await(promise);
    }
}
