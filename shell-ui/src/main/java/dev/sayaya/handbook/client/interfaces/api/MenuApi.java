package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.usecase.MenuRepository;
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

@Singleton
public class MenuApi implements MenuRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    @Inject MenuApi(FetchApi fetchApi, Observer<Progress> progress) {
        this.fetchApi = fetchApi;
        this.progress = progress;
    }

    @Override
    public Observable<List<Menu>> findAll() {
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        Promise<List<Menu>> promise = fetchApi.request("menus", request)
                .then(this::handleResponse)
                .then(this::parse)
                .finally_(()-> {
                    progress.next(Progress.builder().enabled(false).build());
                }).catch_(this::handleException);
        return AsyncSubject.await(promise);
    }
    private Promise<List<Menu>> parse(Response response) {
        try {
            return response.json()
                    .then(values-> Promise.resolve((Menu[])values))
                    .then(values-> Promise.resolve(Arrays.asList(values)));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response: " + e.getMessage());
        }
    }
    private Promise<Response> handleResponse(Response response) {
        return switch (response.status) {
            case 200 -> Promise.resolve(response);
            case 204 -> Promise.reject("Empty result");
            default  -> Promise.reject("HTTP Error: " + response.status + " - " + response.statusText);
        };
    }
    private <V> V handleException(Object throwable) {
        throw new RuntimeException("Search request failed: " + throwable);
    }
}
