package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.usecase.UriStore;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.DomGlobal;
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
 * <p>사용자의 워크스페이스가 0개인 경우 서버(workspace-query)는 302 Found 응답을 반환한다.
 * {@link WorkspaceApi}는 이를 감지하여 {@link UriStore}를 통해 온보딩 화면으로 이동시킨다.</p>
 */
@Singleton
public class WorkspaceApi implements WorkspaceRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    private final UriStore uriStore;

    @Inject public WorkspaceApi(FetchApi fetchApi, Observer<Progress> progress, UriStore uriStore) {
        this.fetchApi = fetchApi;
        this.progress = progress;
        this.uriStore = uriStore;
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
        // 브라우저에 의해 이미 리다이렉트된 경우 (redirected=true)
        if (response.redirected && response.url != null && response.url.contains("/onboarding")) {
            uriStore.next("/workspaces/onboarding");
            return Promise.resolve(List.of());
        }
        return switch (response.status) {
            case 200 -> Promise.resolve(response).then(this::parse);
            case 401, 204 -> Promise.resolve(List.of());
            case 302 -> {
                String location = response.headers.get("Location");
                if (location != null && location.contains("/onboarding")) {
                    uriStore.next("/workspaces/onboarding");
                }
                yield Promise.resolve(List.of());
            }
            default  -> Promise.reject("HTTP Error: " + response.status + " - " + response.statusText);
        };
    }

    private <V> V handleException(Object throwable) {
        progress.next(Progress.hide());
        GWT.log("[handbook-error] WorkspaceApi request failed: " + throwable);
        return null;
    }
}
