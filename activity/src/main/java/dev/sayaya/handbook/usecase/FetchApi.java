package dev.sayaya.handbook.usecase;

import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

/**
 * 브라우저 Fetch API를 래핑한 HTTP 요청 인터페이스.
 */
public interface FetchApi {
    Promise<Response> request(String url, RequestInit param);
    
    default Promise<Response> request(String url) {
        return request(url, null);
    }
}
