package dev.sayaya.handbook.client.api;

import dev.sayaya.handbook.usecase.FetchApi;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FetchApi 구현체로서 URL별 모의 응답을 동적으로 반환한다.
 */
public class FetchMock implements FetchApi {
    @JsFunction
    public interface JsonSupplier {
        Promise<Object> call();
    }

    private final Map<String, Response> mocks = new LinkedHashMap<>();

    public FetchMock() {
        // 기존 하드코딩된 응답을 기본값으로 세팅 (하위 호환성 유지)
        when("workspaces", 200, createWorkspacesJson());
        when("user", 200, createUserJson());
        when("menus", 200, createMenuJson());
        when("auth/refresh", 200, null);
    }

    public FetchMock when(String pathContains, int status, Object jsonPayload) {
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", status);
        mock.set("statusText", status == 200 ? "OK" : "Error");
        mock.set("ok", status >= 200 && status < 300);
        mock.set("json", (JsonSupplier) () -> Promise.resolve(jsonPayload));
        mocks.put(pathContains, Js.cast(mock));
        return this;
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
        return Promise.resolve(createEmptyResponse(404));
    }

    @Override
    public Promise<Response> request(String url) {
        return request(url, null);
    }

    private static Object createUserJson() {
        JsPropertyMap<Object> userObj = JsPropertyMap.of();
        userObj.set("id", "test-user-id");
        userObj.set("name", "TestUser");
        return userObj;
    }

    private static Object createWorkspacesJson() {
        JsPropertyMap<Object> ws = JsPropertyMap.of();
        ws.set("id", "ws-1");
        ws.set("name", "TestWorkspace");
        return new Object[] { ws };
    }

    private static Object createMenuJson() {
        JsPropertyMap<Object> menu1 = JsPropertyMap.of();
        menu1.set("title", "TestMenu");
        menu1.set("order", "A");
        menu1.set("icon", "fa-circle");
        menu1.set("iconType", "sharp");
        menu1.set("script", "js/test.js");
        menu1.set("urls", new String[] { "test-tool" });
        JsPropertyMap<Object> tool1 = JsPropertyMap.of();
        tool1.set("title", "test-tool");
        tool1.set("order", "AA");
        tool1.set("icon", "fa-circle");
        tool1.set("iconType", "sharp");
        menu1.set("tools", new Object[] { tool1 });

        return new Object[] { menu1 };
    }

    private static Response createEmptyResponse(int status) {
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", status);
        mock.set("statusText", "Not Found");
        mock.set("ok", false);
        mock.set("json", (JsonSupplier) () -> Promise.resolve((Object) null));
        return Js.cast(mock);
    }
}
