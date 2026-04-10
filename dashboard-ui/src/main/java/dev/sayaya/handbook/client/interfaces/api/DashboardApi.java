package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.domain.AgentActivity;
import dev.sayaya.handbook.client.domain.QualityIssue;
import dev.sayaya.handbook.client.domain.WorkspaceStats;
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
 * <p><b>주의:</b> 요청 실패 시 null 또는 빈 배열을 반환하며, GWT.log로 오류를 기록한다.</p>
 */
@Singleton
public class DashboardApi implements DashboardRepository {
    private final FetchApi fetchApi;

    @Inject
    public DashboardApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    @Override
    public Observable<WorkspaceStats> fetchStats() {
        Promise<WorkspaceStats> promise = fetchApi.request("dashboard/stats")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<WorkspaceStats>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.fetchStats failed: " + err);
                    return Promise.resolve((WorkspaceStats) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<QualityIssue[]> fetchQualityIssues() {
        Promise<QualityIssue[]> promise = fetchApi.request("dashboard/quality-issues")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<QualityIssue[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.fetchQualityIssues failed: " + err);
                    return Promise.resolve(new QualityIssue[0]);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<AgentActivity[]> fetchAgentActivity() {
        Promise<AgentActivity[]> promise = fetchApi.request("dashboard/agent-activity")
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<AgentActivity[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DashboardApi.fetchAgentActivity failed: " + err);
                    return Promise.resolve(new AgentActivity[0]);
                });
        return AsyncSubject.await(promise);
    }
}
