package dev.sayaya.handbook.client.interfaces.create.api;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.interfaces.create.WorkspaceRepository;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.core.Global.JSON;

@Singleton
public class CreateWorkspaceApi implements WorkspaceRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    @Inject CreateWorkspaceApi(FetchApi fetchApi, Observer<Progress> progress) {
        this.fetchApi = fetchApi;
        this.progress = progress;
    }
    @Override
    public Observable<String> create(String name) {
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var request = RequestInit.create();
        request.setMethod("POST");
        request.setHeaders(new String[][] {
                new String[] {"Content-Type", "application/vnd.sayaya.handbook.v1+json"}
        });
        var param = Js.asPropertyMap(new Object());
        param.set("name", name);
        request.setBody(JSON.stringify(param));
        return AsyncSubject.await(fetchApi
                .request("workspace", request)
                .then(this::handleResponse)
                .then(this::parse)
                .finally_(()-> progress.next(Progress.builder().enabled(false).build()))
                .catch_(this::handleException)
        );
    }

    @Override
    public Observable<String> join(String id) {
        return null;
    }

    private Promise<String> parse(Response response) {
        try {
            return response.text();
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
        throw new RuntimeException("Workspace request failed: " + throwable);
    }
}
