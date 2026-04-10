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

/** DashboardRepository 구현. FetchApi를 사용하여 HTTP 요청을 보낸다. */
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
