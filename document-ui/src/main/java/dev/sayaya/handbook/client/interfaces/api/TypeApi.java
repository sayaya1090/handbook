package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class TypeApi {
    private final FetchApi fetchApi;

    @Inject
    TypeApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    public Observable<Set<Type>> list(LayoutPeriod period) {
        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setHeaders(new String[][]{{"Accept", "application/vnd.sayaya.handbook.v1+json"}});

        Promise<Set<Type>> promise = fetchApi.request("workspaces/types", init)
                .then(this::handleResponse)
                .then(resp -> resp.json())
                .then(json -> {
                    Type[] arr = Js.cast(json);
                    Set<Type> set = new HashSet<>();
                    if (arr != null) {
                        for (Type n : arr) set.add(n);
                    }
                    return Promise.resolve(set);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.list failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    private Promise<Response> handleResponse(Response response) {
        if (response.ok) return Promise.resolve(response);
        return Promise.reject("HTTP " + response.status);
    }
}
