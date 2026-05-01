package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.domain.DocumentValue;
import dev.sayaya.handbook.usecase.DocumentRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.core.Global;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * {@link DocumentRepository} 포트의 HTTP 어댑터.
 *
 * <p><b>책임:</b> Fetch API를 사용하여 document-command 백엔드와 통신.
 * search(GET), save(PUT), patch(PATCH), delete(DELETE) 엔드포인트 호출.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link FetchApi} — HTTP 요청 실행 (인증 쿠키 자동 포함)</li>
 * </ul></p>
 *
 * <p><b>주의:</b> workspace는 {@link #setWorkspace(String)}으로 설정해야 한다.
 * patch() 실패 시 409 Conflict를 Promise.reject로 전파하여 호출자가 충돌 UI를 표시할 수 있게 한다.
 * Content-Type은 항상 {@code application/vnd.sayaya.handbook.v1+json}.</p>
 */
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
        String url = "workspaces/" + workspace + "/documents?page=" + page + "&limit=" + limit + "&type=" + type;
        Promise<DocumentValue[]> promise = fetchApi.request(url)
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<DocumentValue[]>cast(json)))
                .catch_(err -> {
                    GWT.log("DocumentApi.search failed: " + err);
                    ErrorNotifier.notify("DocumentApi.search failed: " + err);
                    return Promise.resolve(new DocumentValue[0]);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> save(List<DocumentValue> documents) {
        String url = "workspaces/" + workspace + "/documents";
        RequestInit init = RequestInit.create();
        init.setMethod("PUT");
        init.setHeaders(jsonHeaders());
        init.setBody(Global.JSON.stringify(documents.toArray()));
        Promise<Void> promise = fetchApi.request(url, init)
                .then(r -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("DocumentApi.save failed: " + err);
                    ErrorNotifier.notify("DocumentApi.save failed: " + err);
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> patch(List<JsPropertyMap<?>> patches) {
        String url = "workspaces/" + workspace + "/documents";
        RequestInit init = RequestInit.create();
        init.setMethod("PATCH");
        init.setHeaders(jsonHeaders());
        init.setBody(Global.JSON.stringify(patches.toArray()));
        Promise<Void> promise = fetchApi.request(url, init)
                .then(r -> {
                    if (r.status == 409) return Promise.reject("Conflict");
                    return Promise.resolve((Void) null);
                })
                .catch_(err -> {
                    GWT.log("DocumentApi.patch failed: " + err);
                    ErrorNotifier.notify("DocumentApi.patch failed: " + err);
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> delete(List<DocumentValue> documents) {
        String url = "workspaces/" + workspace + "/documents";
        RequestInit init = RequestInit.create();
        init.setMethod("DELETE");
        init.setHeaders(jsonHeaders());
        init.setBody(Global.JSON.stringify(documents.toArray()));
        Promise<Void> promise = fetchApi.request(url, init)
                .then(r -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("DocumentApi.delete failed: " + err);
                    ErrorNotifier.notify("DocumentApi.delete failed: " + err);
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
