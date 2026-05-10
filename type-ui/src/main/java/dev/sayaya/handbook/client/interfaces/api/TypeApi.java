package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * TypeRepository의 HTTP 어댑터 구현체.
 *
 * <p><b>책임:</b> REST API를 통해 타입 목록 조회(list), 전체 저장(save), 부분 패치(patch),
 * 삭제(delete) 요청을 수행하고, 응답 JSON을 {@link Type} 도메인 객체로 변환한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link FetchApi} — HTTP 요청 실행</li>
 *   <li>{@link dev.sayaya.handbook.domain.Progress} — 요청 시작/종료 시 프로그레스 바 제어</li>
 * </ul></p>
 * <p><b>주의:</b> workspace 필드를 setWorkspace()로 설정한 후에 API 호출해야 한다.</p>
 */
@Singleton
public class TypeApi implements TypeRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    private String workspace;

    @Inject TypeApi(FetchApi fetchApi, Observer<Progress> progress) {
        this.fetchApi = fetchApi;
        this.progress = progress;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public Observable<Set<Type>> list(LayoutPeriod period) {
        progress.next(Progress.indeterminate());
        String url = "workspaces/" + workspace + "/types?effect_date_time=" + period.effectDateTime() + "&expire_date_time=" + period.expireDateTime();
        Promise<Set<Type>> promise = fetchApi.request(url)
                .then(this::handleResponse)
                .then(Response::json)
                .then(json -> {
                    Type[] arr = Js.cast(json);
                    Set<Type> result = new LinkedHashSet<>();
                    if (arr != null) {
                        for (Type n : arr) result.add(n);
                    }
                    progress.next(Progress.hide());
                    return Promise.resolve(result);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.list failed: " + err);
                    ErrorNotifier.notify("TypeApi.list failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.resolve(new LinkedHashSet<>());
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Set<Type>> save(Set<Type> types) {
        progress.next(Progress.indeterminate());
        Type[] natives = types.toArray(new Type[0]);

        RequestInit init = RequestInit.create();
        init.setMethod("PUT");
        init.setBody(Global.JSON.stringify(natives));
        init.setHeaders(jsonHeaders());

        Promise<Set<Type>> promise = fetchApi.request("workspaces/" + workspace + "/types", init)
                .then(this::handleResponse)
                .then(Response::json)
                .then(json -> {
                    JsArray<Type> arr = Js.cast(json);
                    Set<Type> result = new LinkedHashSet<>();
                    for (int j = 0; j < arr.length; j++) {
                        result.add(arr.getAt(j));
                    }
                    progress.next(Progress.hide());
                    return Promise.resolve(result);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.save failed: " + err);
                    ErrorNotifier.notify("TypeApi.save failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.resolve(new LinkedHashSet<>());
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Set<Type>> patch(List<JsPropertyMap<?>> patches) {
        progress.next(Progress.indeterminate());
        RequestInit init = RequestInit.create();
        init.setMethod("PATCH");
        init.setBody(Global.JSON.stringify(patches.toArray()));
        init.setHeaders(jsonHeaders());

        Promise<Set<Type>> promise = fetchApi.request("workspaces/" + workspace + "/types", init)
                .then(this::handleResponse)
                .then(Response::json)
                .then(json -> {
                    JsArray<Type> arr = Js.cast(json);
                    Set<Type> result = new LinkedHashSet<>();
                    for (int j = 0; j < arr.length; j++) {
                        result.add(arr.getAt(j));
                    }
                    progress.next(Progress.hide());
                    return Promise.resolve(result);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.patch failed: " + err);
                    ErrorNotifier.notify("TypeApi.patch failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.reject(err);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> delete(Set<Type> types) {
        progress.next(Progress.indeterminate());
        Type[] natives = types.toArray(new Type[0]);

        RequestInit init = RequestInit.create();
        init.setMethod("DELETE");
        init.setBody(Global.JSON.stringify(natives));
        init.setHeaders(jsonHeaders());

        Promise<Void> promise = fetchApi.request("workspaces/" + workspace + "/types", init)
                .then(resp -> {
                    progress.next(Progress.hide());
                    return Promise.resolve((Void) null);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.delete failed: " + err);
                    ErrorNotifier.notify("TypeApi.delete failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> patchSchema(dev.sayaya.handbook.domain.SchemaPatch patch) {
        progress.next(Progress.indeterminate());
        RequestInit init = RequestInit.create();
        init.setMethod("PATCH");
        init.setBody(Global.JSON.stringify(patch));
        init.setHeaders(jsonHeaders());

        Promise<Void> promise = fetchApi.request("workspaces/" + workspace + "/schema", init)
                .then(resp -> {
                    progress.next(Progress.hide());
                    return Promise.resolve((Void) null);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.patchSchema failed: " + err);
                    ErrorNotifier.notify("TypeApi.patchSchema failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Set<Type>> versions(String typeId) {
        progress.next(Progress.indeterminate());
        String url = "workspaces/" + workspace + "/types/" + typeId + "/versions";
        Promise<Set<Type>> promise = fetchApi.request(url)
                .then(this::handleResponse)
                .then(Response::json)
                .then(json -> {
                    JsArray<Type> arr = Js.cast(json);
                    Set<Type> result = new LinkedHashSet<>();
                    for (int i = 0; i < arr.length; i++) {
                        result.add(arr.getAt(i));
                    }
                    progress.next(Progress.hide());
                    return Promise.resolve(result);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.versions failed: " + err);
                    ErrorNotifier.notify("TypeApi.versions failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.resolve(new LinkedHashSet<>());
                });
        return AsyncSubject.await(promise);
    }

    private Promise<Response> handleResponse(Response response) {
        if (response.ok) return Promise.resolve(response);
        return Promise.reject("HTTP " + response.status);
    }

    private static Headers jsonHeaders() {
        Headers h = new Headers();
        h.append("Content-Type", "application/vnd.sayaya.handbook.v1+json");
        return h;
    }
}
