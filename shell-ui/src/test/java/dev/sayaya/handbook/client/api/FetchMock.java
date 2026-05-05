package dev.sayaya.handbook.client.api;

import dev.sayaya.handbook.usecase.FetchApi;
import elemental2.dom.Response;
import elemental2.dom.ResponseInit;
import elemental2.dom.RequestInit;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FetchApi 구현체로서 URL별 모의 응답을 동적으로 반환한다.
 * 실제 네트워크 요청(window.fetch)을 호출하지 않고 native Response 객체를 생성하여 반환한다.
 */
public class FetchMock implements FetchApi {
    private final Map<String, Response> mocks = new LinkedHashMap<>();

    public FetchMock() {
        // 기본 데이터 모킹 (8888 포트 실제 요청 방지)
        when("user", 200, "{}");
        when("menus", 200, "[]");
        when("auth/refresh", 200, "null");
    }

    /**
     * 특정 경로가 포함된 요청에 대해 모의 응답을 설정한다.
     * body 는 JSON 문자열이어야 함.
     */
    public FetchMock when(String pathContains, int status, String bodyJson, Map<String, String> headers) {
        mocks.put(pathContains, createMockResponse(status, false, pathContains, bodyJson, headers));
        return this;
    }

    public FetchMock when(String pathContains, int status, String bodyJson) {
        return when(pathContains, status, bodyJson, null);
    }

    public FetchMock when(String pathContains, Response response) {
        mocks.put(pathContains, response);
        return this;
    }

    @Override
    public Promise<Response> request(String url, RequestInit param) {
        if (url != null) {
            for (Map.Entry<String, Response> entry : mocks.entrySet()) {
                if (url.contains(entry.getKey())) {
                    return Promise.resolve(entry.getValue());
                }
            }
        }
        return Promise.resolve(createMockResponse(404, false, url, null, null));
    }

    @Override
    public Promise<Response> request(String url) {
        return request(url, null);
    }

    /**
     * 실제 브라우저의 Response 생성자를 사용하여 객체를 만든다. (ClassCastException 방지)
     */
    public static Response createMockResponse(int status, boolean redirected, String url, String bodyJson, Map<String, String> headers) {
        ResponseInit init = ResponseInit.create();
        init.setStatus(status);
        
        // Response 바디가 null 인 경우 빈 문자열 처리
        String body = bodyJson != null ? bodyJson : "";
        Response response = new Response(body, init);
        
        // redirected, url 속성은 read-only 이므로 런타임에 강제 주입
        elemental2.core.Reflect.defineProperty(response, "redirected", jsinterop.base.Js.uncheckedCast(jsinterop.base.JsPropertyMap.of("value", redirected, "configurable", true)));
        elemental2.core.Reflect.defineProperty(response, "url", jsinterop.base.Js.uncheckedCast(jsinterop.base.JsPropertyMap.of("value", url, "configurable", true)));
        
        if (headers != null) {
            var h = jsinterop.base.Js.asPropertyMap(response.headers);
            headers.forEach(h::set);
        }
        
        return response;
    }
}
