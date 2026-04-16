package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserApi implements UserRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    @Inject UserApi(FetchApi fetchApi, Observer<Progress> progress) {
        this.fetchApi = fetchApi;
        this.progress = progress;
    }

    @Override
    public Observable<User> find() {
        progress.next(Progress.indeterminate());
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        Promise<User> promise = fetchApi.request("user", request)
                .then(this::handleResponse)
                .then(result -> { progress.next(Progress.hide()); return Promise.resolve(result); })
                .catch_(this::handleException);
        return AsyncSubject.await(promise).tap(this::periodicRefresh);
    }
    private final static int REFRESH_INTERVAL = 10 * 1000 * 60; // 10 minutes
    private Subscription periodicRefreshSubscription;
    private void periodicRefresh(User user) {
        if (periodicRefreshSubscription != null) {
            periodicRefreshSubscription.unsubscribe();
            periodicRefreshSubscription = null;
        }
        if (user != null) {
            periodicRefreshSubscription = Observable.timer(REFRESH_INTERVAL, REFRESH_INTERVAL).subscribe(i -> {
                try {
                    refresh();
                } catch (Exception e) {
                    DomGlobal.console.error("Failed to refresh: " + e.getMessage());
                    periodicRefreshSubscription.unsubscribe();
                    periodicRefreshSubscription = null;
                }
            });
        }
    }
    private Observable<Void> refresh() {
        var request = RequestInit.create();
        Promise<Void> promise = fetchApi.request("auth/refresh", request)
                .then(response -> Promise.resolve((Void) null))
                .catch_(this::handleException);
        return AsyncSubject.await(promise);
    }
    private Promise<User> parse(Response response) {
        try {
            return response.json().then(values -> Promise.resolve((User) values));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response: " + e.getMessage());
        }
    }
    private Promise<User> handleResponse(Response response) {
        return switch (response.status) {
            case 200 -> Promise.resolve(response).then(this::parse);
            case 401, 500 -> {
                // 미인증 또는 서버 에러 — 로그인 페이지로 리다이렉트.
                // 에러 다이얼로그를 띄우면 전체 화면을 덮어 사용자가 아무것도 클릭할 수 없다.
                DomGlobal.window.location.assign("/auth/login");
                yield Promise.resolve((User) null);
            }
            case 204 -> Promise.reject("Empty result");
            default  -> Promise.reject("HTTP Error: " + response.status + " - " + response.statusText);
        };
    }
    private <V> V handleException(Object throwable) {
        progress.next(Progress.hide());
        DomGlobal.window.location.assign("/auth/login");
        throw new RuntimeException("User request failed: " + throwable);
    }
}
