package dev.sayaya.handbook.usecase;

import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

/**
 * 브라우저 Fetch API를 래핑한 HTTP 요청 인터페이스.
 *
 * <p><b>책임:</b> DomGlobal.fetch()를 감싸서 URL과 RequestInit를 받아 Promise&lt;Response&gt;를 반환한다.</p>
 * <p><b>의존관계:</b> <ul><li>{@link elemental2.dom.DomGlobal#fetch} — 브라우저 네이티브 fetch API</li></ul></p>
 */
public interface FetchApi {
    default Promise<Response> request(String url) {
        return request(url, null);
    }
    default Promise<Response> request(String url, RequestInit param) {
        return DomGlobal.fetch(url, param);
    }
}
