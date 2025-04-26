package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static elemental2.core.Global.JSON;

@SuppressWarnings("SimplifyStreamApiCallChains")
@Singleton
public class TypeApi implements SearchApi<TypeNative>, TypeRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    private Workspace workspace;
    @Inject TypeApi(FetchApi fetchApi, Observer<Progress> progress, Observable<Workspace> workspace) {
        this.fetchApi = fetchApi;
        this.progress = progress;
        workspace.distinctUntilChanged().subscribe(w-> this.workspace = w);
    }
    @Override
    public Promise<Response> searchRequest(String url) {
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        return fetchApi.request(url, request);
    }
    @Override
    public Observable<Void> save(List<Box> boxes) {
        if(workspace==null) return Observable.of((Void)null);
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var request = RequestInit.create();
        request.setMethod("POST");
        request.setHeaders(new String[][] {
                new String[] {"Content-Type", "application/vnd.sayaya.handbook.v1+json"}
        });
        request.setBody(JSON.stringify(boxes.stream().toArray(Box[]::new)));
        return AsyncSubject.await(fetchApi
                .request("workspace", request)
                .then(this::handleResponse)
                .then(this::parse)
                .finally_(()-> progress.next(Progress.builder().enabled(false).build()))
                .catch_(this::handleException)
        );
    }
    private Promise<Void> parse(Response response) {
        return Promise.resolve((Void)null);
    }
    private Promise<Response> handleResponse(Response response) {
        return switch (response.status) {
            case 200 -> Promise.resolve(response);
            case 204 -> Promise.reject("Empty result");
            default  -> Promise.reject("HTTP Error: " + response.status + " - " + response.statusText);
        };
    }
    private <V> V handleException(Object throwable) {
        throw new RuntimeException("Request failed: " + throwable);
    }
    @Override
    public Observable<List<Type>> list() {
        if(workspace==null) return Observable.of(List.of());
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var promise = search("workspace/" + workspace.id() + "/types", Search.builder().limit(100).build()).finally_(()-> progress.next(Progress.builder().enabled(false).build()));
        return AsyncSubject.await(promise).map(page-> {
            var natives = page.content();
            return Arrays.stream(natives).map(TypeNative::toType).collect(Collectors.toList());
        });
    }
}
