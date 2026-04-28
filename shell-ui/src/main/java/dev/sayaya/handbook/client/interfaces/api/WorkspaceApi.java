package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

/**
 * {@link WorkspaceRepository} 구현 — {@code GET /workspaces} 를 구독해 현재 사용자의
 * 소속 워크스페이스 목록을 반환한다. 응답 필터링은 workspace-query 가 principal.sub
 * 기반으로 수행하며, 프론트는 결과 그대로 소비한다.
 *
 * <p>{@link UserApi} 패턴 복제 — {@link FetchApi} 를 통한 HTTP 호출 + {@link Progress}
 * 이벤트 발행. 응답이 JSON 배열이므로 {@code Js.cast} 로 {@code Workspace[]} 바인딩.</p>
 */
@Singleton
public class WorkspaceApi implements WorkspaceRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    @Inject WorkspaceApi(FetchApi fetchApi, Observer<Progress> progress) {
        this.fetchApi = fetchApi;
        this.progress = progress;
    }

    @Override
    public Observable<List<Workspace>> list() {
        progress.next(Progress.indeterminate());
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        Promise<List<Workspace>> promise = fetchApi.request("workspaces", request)
                .then(this::handleResponse)
                .then(result -> { progress.next(Progress.hide()); return Promise.resolve(result); })
                .catch_(this::handleException);
        return AsyncSubject.await(promise);
    }

    private Promise<List<Workspace>> parse(Response response) {
        try {
            return response.json().then(values -> {
                Workspace[] arr = Js.cast(values);
                return Promise.resolve(arr == null ? List.of() : Arrays.asList(arr));
            });
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response: " + e.getMessage());
        }
    }
    private Promise<List<Workspace>> handleResponse(Response response) {
        return switch (response.status) {
            case 200 -> Promise.resolve(response).then(this::parse);
            case 401 -> Promise.resolve(List.of());
            case 204 -> Promise.resolve(List.of());
            default  -> Promise.reject("HTTP Error: " + response.status + " - " + response.statusText);
        };
    }
    private <V> V handleException(Object throwable) {
        progress.next(Progress.hide());
        GWT.log("[handbook-error] WorkspaceApi request failed: " + throwable);
        return null;
    }
}
