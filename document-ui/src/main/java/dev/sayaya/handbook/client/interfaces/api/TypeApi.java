package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.domain.TypeInfo;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;

/** TypeRepository 구현. 타입 목록을 가져와 컬럼 정의에 사용한다. */
@Singleton
public class TypeApi implements TypeRepository {
    private final FetchApi fetchApi;
    private String workspace;

    @Inject
    public TypeApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public Observable<TypeInfo[]> list() {
        String url = "workspace/" + workspace + "/types";
        Promise<TypeInfo[]> promise = fetchApi.request(url)
                .then(Response::json)
                .then(json -> Promise.resolve(Js.<TypeInfo[]>cast(json)))
                .catch_(err -> {
                    GWT.log("TypeApi.list failed: " + err);
                    return Promise.resolve(new TypeInfo[0]);
                });
        return AsyncSubject.await(promise);
    }
}
