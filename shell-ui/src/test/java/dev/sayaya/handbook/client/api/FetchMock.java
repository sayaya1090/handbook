package dev.sayaya.handbook.client.api;

import dev.sayaya.handbook.usecase.FetchApi;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * FetchApi 구현체로서 URL별 모의 응답을 반환한다.
 * GWT 환경에서 elemental2 Response는 네이티브 JS 객체이므로
 * JsPropertyMap + Js.cast 패턴으로 생성한다.
 */
public class FetchMock implements FetchApi {
    @JsFunction
    public interface JsonSupplier {
        Promise<Object> call();
    }

    @Override
    public Promise<Response> request(String url, RequestInit param) {
        if (url != null && url.contains("workspaces")) {
            return Promise.resolve(createWorkspacesResponse());
        }
        if (url != null && url.contains("user")) {
            return Promise.resolve(createUserResponse());
        }
        if (url != null && url.contains("menus")) {
            return Promise.resolve(createMenuResponse());
        }
        if (url != null && url.contains("auth/refresh")) {
            return Promise.resolve(createRefreshResponse());
        }
        return Promise.resolve(createEmptyResponse(404));
    }

    private static Response createUserResponse() {
        JsPropertyMap<Object> userObj = JsPropertyMap.of();
        userObj.set("id", "test-user-id");
        userObj.set("name", "TestUser");

        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", 200);
        mock.set("statusText", "OK");
        mock.set("json", (JsonSupplier) () -> Promise.resolve(userObj));
        return Js.cast(mock);
    }

    private static Response createWorkspacesResponse() {
        JsPropertyMap<Object> ws = JsPropertyMap.of();
        ws.set("id", "ws-1");
        ws.set("name", "TestWorkspace");
        Object[] arr = new Object[] { ws };

        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", 200);
        mock.set("statusText", "OK");
        mock.set("json", (JsonSupplier) () -> Promise.resolve(arr));
        return Js.cast(mock);
    }

    private static Response createMenuResponse() {
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

        Object[] menus = new Object[] { menu1 };

        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", 200);
        mock.set("statusText", "OK");
        mock.set("json", (JsonSupplier) () -> Promise.resolve(menus));
        return Js.cast(mock);
    }

    private static Response createRefreshResponse() {
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", 200);
        mock.set("statusText", "OK");
        mock.set("json", (JsonSupplier) () -> Promise.resolve((Object) null));
        return Js.cast(mock);
    }

    private static Response createEmptyResponse(int status) {
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", status);
        mock.set("statusText", "Not Found");
        mock.set("json", (JsonSupplier) () -> Promise.resolve((Object) null));
        return Js.cast(mock);
    }
}
