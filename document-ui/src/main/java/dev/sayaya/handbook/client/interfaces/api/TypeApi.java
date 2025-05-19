package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.TypeList;
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

@Singleton
public class TypeApi implements TypeRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    private final TypeList typeList;
    private Workspace workspace;
    @Inject TypeApi(FetchApi fetchApi, Observer<Progress> progress, Observable<Workspace> workspace, TypeList typeList) {
        this.fetchApi = fetchApi;
        this.progress = progress;
        this.typeList = typeList;
        workspace.distinctUntilChanged().subscribe(w-> this.workspace = w);
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
    public void initialize() {
        list().subscribe(typeList::next);
    }
    @Override
    public Observable<List<Type>> list() {
        if(workspace==null) return Observable.of(List.of());
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        return AsyncSubject.await(fetchApi
                .request("workspace/" + workspace.id() + "/types", request)
                .then(this::handleResponse)
                .then(this::parse)
                .finally_(()-> progress.next(Progress.builder().enabled(false).build()))
                .catch_(this::handleException)
        );
    }
    private Promise<List<Type>> parse(Response response) {
        return response.json().then(values -> {
            var natives = (TypeNative[]) values;
            var list = Arrays.stream(natives).map(TypeNative::toDomain).collect(Collectors.toList());
            return Promise.resolve(list);
        });
    }
}
