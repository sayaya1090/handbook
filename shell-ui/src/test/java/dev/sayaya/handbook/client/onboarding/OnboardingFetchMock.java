package dev.sayaya.handbook.client.onboarding;

import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.domain.Menu;
import elemental2.core.JsArray;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import static elemental2.dom.DomGlobal.window;

public class OnboardingFetchMock implements FetchApi {
    @JsFunction public interface JsonSupplier { Promise<Object> call(); }

    @Override
    public Promise<Response> request(String url, RequestInit param) {
        JsPropertyMap<Object> win = Js.asPropertyMap(window);
        
        if (url != null && url.contains("workspaces")) {
            // 전역 변수에 따라 워크스페이스 유무 결정
            if ("IN_WORKSPACE".equals(win.get("test_session_mode"))) {
                return Promise.resolve(createWorkspaceResponse());
            } else {
                return Promise.resolve(createEmptyArrayResponse());
            }
        }
        if (url != null && url.contains("user")) {
            return Promise.resolve(createUserResponse());
        }
        if (url != null && url.contains("menus")) {
            if ("EMPTY".equals(win.get("test_menu_mode"))) return Promise.resolve(createEmptyArrayResponse());
            else return Promise.resolve(createOnboardingMenuResponse());
        }
        return Promise.resolve(createEmptyResponse(200));
    }

    private static Response createUserResponse() {
        JsPropertyMap<Object> user = JsPropertyMap.of();
        user.set("id", "user-1");
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", 200);
        mock.set("json", (JsonSupplier) () -> Promise.resolve(user));
        return Js.cast(mock);
    }

    private static Response createWorkspaceResponse() {
        JsPropertyMap<Object> ws = JsPropertyMap.of();
        ws.set("id", "ws-1");
        JsArray<Object> list = new JsArray<>();
        list.push(ws);
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", 200);
        mock.set("json", (JsonSupplier) () -> Promise.resolve(list));
        return Js.cast(mock);
    }

    private static Response createOnboardingMenuResponse() {
        JsPropertyMap<Object> menu = JsPropertyMap.of();
        menu.set("title", "공간 관리");
        JsArray<String> urls = new JsArray<>();
        urls.push("/workspaces");
        menu.set("url_regex", urls);
        JsArray<Object> list = new JsArray<>();
        list.push(menu);
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", 200);
        mock.set("json", (JsonSupplier) () -> Promise.resolve(list));
        return Js.cast(mock);
    }

    private static Response createEmptyArrayResponse() {
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", 200);
        mock.set("json", (JsonSupplier) () -> Promise.resolve(new JsArray<>()));
        return Js.cast(mock);
    }

    private static Response createEmptyResponse(int status) {
        JsPropertyMap<Object> mock = JsPropertyMap.of();
        mock.set("status", status);
        mock.set("json", (JsonSupplier) () -> Promise.resolve((Object) null));
        return Js.cast(mock);
    }
}
