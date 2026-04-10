package dev.sayaya.handbook.client.interfaces.api;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ErrorNotifier;
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

/**
 * TypeRepository의 HTTP API 구현체.
 *
 * <p><b>책임:</b> {@link dev.sayaya.handbook.usecase.FetchApi}를 사용하여 서버로부터
 * 워크스페이스별 타입 목록을 조회하고, {@link dev.sayaya.handbook.client.domain.TypeInfo} 배열로
 * 반환한다. 조회 결과는 컬럼 정의 생성에 사용된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link dev.sayaya.handbook.usecase.FetchApi} — HTTP 요청 유틸리티</li>
 *   <li>{@link TypeRepository} — 구현 대상 포트 인터페이스</li>
 *   <li>{@link dev.sayaya.rx.subject.AsyncSubject} — Promise를 Observable로 변환</li>
 * </ul></p>
 *
 * <p><b>주의:</b> setWorkspace()로 워크스페이스를 설정한 후 list()를 호출해야 한다.
 * 요청 실패 시 빈 배열을 반환하며, 오류는 GWT.log로 기록된다.</p>
 */
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
                    ErrorNotifier.notify("TypeApi.list failed: " + err);
                    return Promise.resolve(new TypeInfo[0]);
                });
        return AsyncSubject.await(promise);
    }
}
