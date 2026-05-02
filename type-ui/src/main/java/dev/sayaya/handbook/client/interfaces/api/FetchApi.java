package dev.sayaya.handbook.client.interfaces.api;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

public interface FetchApi {
    Promise<Response> request(String url);
    Promise<Response> request(String url, RequestInit init);
}
