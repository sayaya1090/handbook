package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsDate;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.Set;

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
    public Observable<Set<TypeValue>> list(LayoutPeriod period) {
        progress.next(Progress.indeterminate());
        String effectIso = new JsDate(period.effectDateTime).toISOString();
        String expireIso = new JsDate(period.expireDateTime).toISOString();
        String url = "workspace/" + workspace + "/types?effect_date_time=" + effectIso + "&expire_date_time=" + expireIso;
        Promise<Set<TypeValue>> promise = fetchApi.request(url)
                .then(this::handleResponse)
                .then(Response::json)
                .then(json -> {
                    JsArray<TypeNative> arr = Js.cast(json);
                    Set<TypeValue> result = new LinkedHashSet<>();
                    for (int i = 0; i < arr.length; i++) {
                        result.add(arr.getAt(i).toDomain());
                    }
                    progress.next(Progress.hide());
                    return Promise.resolve(result);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.list failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.resolve(new LinkedHashSet<>());
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Set<TypeValue>> save(Set<TypeValue> types) {
        progress.next(Progress.indeterminate());
        TypeNative[] natives = new TypeNative[types.size()];
        int i = 0;
        for (TypeValue t : types) natives[i++] = TypeNative.fromDomain(t);

        RequestInit init = RequestInit.create();
        init.setMethod("PUT");
        init.setBody(Global.JSON.stringify(natives));
        init.setHeaders(new String[][]{
                {"Content-Type", "application/vnd.sayaya.handbook.v1+json"}
        });

        Promise<Set<TypeValue>> promise = fetchApi.request("workspace/" + workspace + "/types", init)
                .then(this::handleResponse)
                .then(Response::json)
                .then(json -> {
                    JsArray<TypeNative> arr = Js.cast(json);
                    Set<TypeValue> result = new LinkedHashSet<>();
                    for (int j = 0; j < arr.length; j++) {
                        result.add(arr.getAt(j).toDomain());
                    }
                    progress.next(Progress.hide());
                    return Promise.resolve(result);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.save failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.resolve(new LinkedHashSet<>());
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> delete(Set<TypeValue> types) {
        progress.next(Progress.indeterminate());
        TypeNative[] natives = new TypeNative[types.size()];
        int i = 0;
        for (TypeValue t : types) natives[i++] = TypeNative.fromDomain(t);

        RequestInit init = RequestInit.create();
        init.setMethod("DELETE");
        init.setBody(Global.JSON.stringify(natives));
        init.setHeaders(new String[][]{
                {"Content-Type", "application/vnd.sayaya.handbook.v1+json"}
        });

        Promise<Void> promise = fetchApi.request("workspace/" + workspace + "/types", init)
                .then(resp -> {
                    progress.next(Progress.hide());
                    return Promise.resolve((Void) null);
                })
                .catch_(err -> {
                    GWT.log("TypeApi.delete failed: " + err);
                    progress.next(Progress.hide());
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    private Promise<Response> handleResponse(Response response) {
        if (response.ok) return Promise.resolve(response);
        return Promise.reject("HTTP " + response.status);
    }
}
