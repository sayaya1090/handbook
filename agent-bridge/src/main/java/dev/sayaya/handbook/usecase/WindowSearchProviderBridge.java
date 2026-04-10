package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 SearchProvider를 연결하는 브릿지.
 *
 * <p>type-ui는 {@link #register(SearchCallback)}로 검색 콜백을 등록하고,
 * agent-ui는 {@link #search(String)}으로 검색을 실행한다.
 *
 * <p>window 속성: {@code __handbook_searchProvider}
 */
public final class WindowSearchProviderBridge {
    private static final String KEY = "__handbook_searchProvider";

    private WindowSearchProviderBridge() {}

    /** 검색 결과를 동기적으로 반환하는 콜백. */
    public interface SearchCallback {
        String search(String query);
    }

    /** 편집 모듈 측: 검색 콜백을 window에 등록한다. */
    public static void register(SearchCallback callback) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        win.set(KEY, (SearchFn) callback::search);
    }

    /** 에이전트 측: 등록된 검색 콜백을 호출하여 결과를 Observable로 반환한다. */
    public static Observable<String> search(String query) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        Object fn = win.get(KEY);
        String result;
        if (fn != null) {
            result = Js.<SearchFn>cast(fn).call(query);
        } else {
            result = "{\"results\":[]}";
        }
        return BehaviorSubject.<String>behavior(result).asObservable();
    }

    /** SearchCallback이 등록되어 있는지 확인한다. */
    public static boolean isRegistered() {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        return win.has(KEY);
    }

    @jsinterop.annotations.JsFunction
    private interface SearchFn {
        String call(String query);
    }
}
