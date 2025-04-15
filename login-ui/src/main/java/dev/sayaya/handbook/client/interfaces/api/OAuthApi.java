package dev.sayaya.handbook.client.interfaces.api;

import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.dom.DomGlobal.window;

@Singleton
public class OAuthApi {
    private final FetchApi fetchApi;
    @Inject OAuthApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }
    public Promise<Void> login(String provider) {
        var requestUrl = "login/oauth2/authorization/" + provider;
        window.location.href = requestUrl;
        return null;
    }
}
