package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.interfaces.api.LayoutNative;
import dev.sayaya.handbook.client.usecase.LayoutRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class LayoutApi implements LayoutRepository {
    private final FetchApi fetchApi;
    private String workspace;

    @Inject LayoutApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public Observable<List<LayoutPeriod>> layouts() {
        Promise<List<LayoutPeriod>> promise = fetchApi.request("workspaces/" + workspace + "/layouts")
                .then(this::handleResponse)
                .then(Response::json)
                .then(json -> {
                    JsArray<LayoutNative> arr = Js.cast(json);
                    List<LayoutPeriod> result = new ArrayList<>();
                    for (int i = 0; i < arr.length; i++) {
                        result.add(arr.getAt(i).toPeriod());
                    }
                    return Promise.resolve(result);
                })
                .catch_(err -> {
                    GWT.log("LayoutApi.layouts failed: " + err);
                    ErrorNotifier.notify("LayoutApi.layouts failed: " + err);
                    return Promise.resolve(Collections.emptyList());
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Map<String, Position>> positions(LayoutPeriod period) {
        Promise<Map<String, Position>> promise = fetchApi.request("workspaces/" + workspace + "/layouts")
                .then(this::handleResponse)
                .then(Response::json)
                .then(json -> {
                    JsArray<LayoutNative> arr = Js.cast(json);
                    for (int i = 0; i < arr.length; i++) {
                        LayoutNative layout = arr.getAt(i);
                        LayoutPeriod lp = layout.toPeriod();
                        if (lp.effectDateTime == period.effectDateTime && lp.expireDateTime == period.expireDateTime) {
                            return Promise.resolve(layout.toPositionMap());
                        }
                    }
                    return Promise.resolve(Collections.<String, Position>emptyMap());
                })
                .catch_(err -> {
                    GWT.log("LayoutApi.positions failed: " + err);
                    ErrorNotifier.notify("LayoutApi.positions failed: " + err);
                    return Promise.resolve(Collections.emptyMap());
                });
        return AsyncSubject.await(promise);
    }

    @Override
    public Observable<Void> savePositions(LayoutPeriod period, Map<String, Position> positions) {
        // Backend expects TypeLayout { id, effectDateTime, expireDateTime, positions }
        LayoutNative layout = new LayoutNative();
        layout.effectDateTime = new elemental2.core.JsDate((double) period.effectDateTime).toISOString();
        layout.expireDateTime = new elemental2.core.JsDate((double) period.expireDateTime).toISOString();
        jsinterop.base.JsPropertyMap<LayoutNative.PositionNative> posMap = jsinterop.base.JsPropertyMap.of();
        for (Map.Entry<String, Position> entry : positions.entrySet()) {
            LayoutNative.PositionNative pn = new LayoutNative.PositionNative();
            pn.x = entry.getValue().x;
            pn.y = entry.getValue().y;
            pn.width = entry.getValue().width;
            pn.height = entry.getValue().height;
            posMap.set(entry.getKey(), pn);
        }
        layout.positions = posMap;

        RequestInit init = RequestInit.create();
        init.setMethod("PUT");
        init.setBody(Global.JSON.stringify(layout));
        init.setHeaders(new String[][]{
                {"Content-Type", "application/vnd.sayaya.handbook.v1+json"}
        });

        Promise<Void> promise = fetchApi.request("workspaces/" + workspace + "/layouts", init)
                .then(resp -> Promise.resolve((Void) null))
                .catch_(err -> {
                    GWT.log("LayoutApi.savePositions failed: " + err);
                    ErrorNotifier.notify("LayoutApi.savePositions failed: " + err);
                    return Promise.resolve((Void) null);
                });
        return AsyncSubject.await(promise);
    }

    private Promise<Response> handleResponse(Response response) {
        if (response.ok) return Promise.resolve(response);
        return Promise.reject("HTTP " + response.status);
    }
}
