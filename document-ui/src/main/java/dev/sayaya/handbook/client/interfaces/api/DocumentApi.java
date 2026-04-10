package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.client.usecase.DocumentRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.core.Global;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/** DocumentRepository 구현. FetchApi를 사용하여 HTTP 요청을 보낸다. */
@Singleton
public class DocumentApi implements DocumentRepository {
    private final FetchApi fetchApi;
    private String workspace;

    @Inject
    public DocumentApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public Observable<DocumentValue[]> search(String type, int page, int limit) {
        String url = "workspace/" + workspace + "/documents?page=" + page + "&limit=" + limit + "&type=" + type;
        Promise<DocumentValue[]> promise = fetchApi.request(url)
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<DocumentValue[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DocumentApi.search failed: " + err);
                    return Promise.resolve(new DocumentValue[0]);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> save(List<DocumentValue> documents) {
        String url = "workspace/" + workspace + "/documents";
        RequestInit init = RequestInit.create();
        init.setMethod("PUT");
        init.setHeaders(jsonHeaders());
        init.setBody(Global.JSON.stringify(documents.toArray()));
        Promise<Void> promise = fetchApi.request(url, init)
                .then(r -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("DocumentApi.save failed: " + err);
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> delete(List<DocumentValue> documents) {
        String url = "workspace/" + workspace + "/documents";
        RequestInit init = RequestInit.create();
        init.setMethod("DELETE");
        init.setHeaders(jsonHeaders());
        init.setBody(Global.JSON.stringify(documents.toArray()));
        Promise<Void> promise = fetchApi.request(url, init)
                .then(r -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("DocumentApi.delete failed: " + err);
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    private static Headers jsonHeaders() {
        Headers h = new Headers();
        h.append("Content-Type", "application/vnd.sayaya.handbook.v1+json");
        return h;
    }
}
