package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.core.Global;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WorkspaceApi implements WorkspaceRepository {
    private final FetchApi fetchApi;

    @Inject WorkspaceApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    @Override
    public Observable<String> create(String name, String description) {
        JsPropertyMap<Object> body = JsPropertyMap.of();
        body.set("name", name);
        if (description != null) body.set("description", description);

        RequestInit init = RequestInit.create();
        init.setMethod("POST");
        init.setBody(Global.JSON.stringify(body));
        init.setHeaders(new String[][]{
                {"Content-Type", "application/vnd.sayaya.handbook.v1+json"}
        });

        Promise<String> promise = fetchApi.request("workspaces", init)
                .then(this::handleResponse)
                .then(Response::text)
                .catch_(err -> {
                    GWT.log("WorkspaceApi.create failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.create failed: " + err);
                    return Promise.resolve((String) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<String> update(String id, String name, String description) {
        JsPropertyMap<Object> body = JsPropertyMap.of();
        body.set("name", name);
        if (description != null) body.set("description", description);

        RequestInit init = RequestInit.create();
        init.setMethod("PUT");
        init.setBody(Global.JSON.stringify(body));
        init.setHeaders(new String[][]{
                {"Content-Type", "application/vnd.sayaya.handbook.v1+json"}
        });

        Promise<String> promise = fetchApi.request("workspace/" + id, init)
                .then(this::handleResponse)
                .then(Response::text)
                .catch_(err -> {
                    GWT.log("WorkspaceApi.update failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.update failed: " + err);
                    return Promise.resolve((String) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> delete(String id) {
        RequestInit init = RequestInit.create();
        init.setMethod("DELETE");

        Promise<Void> promise = fetchApi.request("workspace/" + id, init)
                .then(resp -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.delete failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.delete failed: " + err);
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> join(String workspaceId) {
        RequestInit init = RequestInit.create();
        init.setMethod("POST");

        Promise<Void> promise = fetchApi.request("workspace/" + workspaceId + "/join", init)
                .then(this::handleResponse)
                .then(resp -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("WorkspaceApi.join failed: " + err);
                    ErrorNotifier.notify("WorkspaceApi.join failed: " + err);
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    private Promise<Response> handleResponse(Response response) {
        if (response.ok) return Promise.resolve(response);
        return Promise.reject("HTTP " + response.status);
    }
}
