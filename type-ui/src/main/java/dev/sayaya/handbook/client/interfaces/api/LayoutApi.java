package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.client.usecase.LayoutRepository;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.core.Global;
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
                    TypeLayout[] arr = Js.cast(json);
                    List<LayoutPeriod> result = new ArrayList<>();
                    if (arr != null) {
                        for (TypeLayout layout : arr) {
                            result.add(layout.toPeriod());
                        }
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
                    TypeLayout[] arr = Js.cast(json);
                    if (arr != null) {
                        for (TypeLayout layout : arr) {
                            LayoutPeriod lp = layout.toPeriod();
                            if (lp.effectDateTime() == period.effectDateTime() && lp.expireDateTime() == period.expireDateTime()) {
                                Map<String, Position> map = new HashMap<>();
                                if (layout.positions() != null) {
                                    layout.positions().forEach(key -> map.put(key, layout.positions().get(key)));
                                }
                                return Promise.resolve(map);
                            }
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
        jsinterop.base.JsPropertyMap<Position> posMap = jsinterop.base.JsPropertyMap.of();
        for (Map.Entry<String, Position> entry : positions.entrySet()) {
            posMap.set(entry.getKey(), entry.getValue());
        }
        
        TypeLayout layout = TypeLayout.create(null, workspace, period.effectDateTime(), period.expireDateTime(), posMap);

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
