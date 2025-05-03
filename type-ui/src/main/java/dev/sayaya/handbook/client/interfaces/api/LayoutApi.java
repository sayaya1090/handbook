package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.LayoutRepository;
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

@SuppressWarnings("SimplifyStreamApiCallChains")
@Singleton
public class LayoutApi implements LayoutRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    private Workspace workspace;
    @Inject LayoutApi(FetchApi fetchApi, Observer<Progress> progress, Observable<Workspace> workspace) {
        this.fetchApi = fetchApi;
        this.progress = progress;
        workspace.distinctUntilChanged().subscribe(w->this.workspace = w);
    }
    @Override
    public Observable<List<Period>> layouts() {
        if(workspace==null) return Observable.of(List.of());
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        return AsyncSubject.await(fetchApi
                .request("workspace/" + workspace.id() + "/layouts", request)
                .then(this::handleResponse)
                .then(this::parse)
                .finally_(()-> progress.next(Progress.builder().enabled(false).build()))
                .catch_(this::handleException)
        );
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
    private Promise<List<Period>> parse(Response response) {
        return response.json().then(values -> {
            var natives = (PeriodNative[]) values;
            var list = Arrays.stream(natives).map(PeriodNative::toPeriod).collect(Collectors.toList());
            return Promise.resolve(list);
        });
    }
}
