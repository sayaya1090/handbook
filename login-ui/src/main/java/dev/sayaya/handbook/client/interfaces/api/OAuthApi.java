package dev.sayaya.handbook.client.interfaces.api;

import elemental2.dom.RequestInit;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OAuthApi {
    private final FetchApi fetchApi;
    @Inject OAuthApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }
    public Promise<Void> logout() {
        var request = RequestInit.create();
        request.setMethod("POST");
        return fetchApi.request("oauth2/logout", request).then(response->Promise.resolve((Void)null));
    }
}
