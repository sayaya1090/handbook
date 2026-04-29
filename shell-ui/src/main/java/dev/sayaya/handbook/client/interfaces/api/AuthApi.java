package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.usecase.AuthRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.RequestInit;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthApi implements AuthRepository {
    private final FetchApi fetchApi;

    @Inject
    public AuthApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    @Override
    public Observable<Boolean> refresh() {
        RequestInit request = RequestInit.create();
        request.setMethod("POST");
        Promise<Boolean> promise = fetchApi.request("auth/refresh", request)
                .then(response -> Promise.resolve(response.ok))
                .catch_(e -> Promise.resolve(false));
        return AsyncSubject.await(promise);
    }
}
