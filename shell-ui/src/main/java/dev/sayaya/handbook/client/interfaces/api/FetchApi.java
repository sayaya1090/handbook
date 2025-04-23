package dev.sayaya.handbook.client.interfaces.api;

import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

public interface FetchApi {
    default Promise<Response> request(String url) {
        return request(url, null);
    }
    default Promise<Response> request(String url, RequestInit param) {
        return DomGlobal.fetch(url, param);
    }
}
