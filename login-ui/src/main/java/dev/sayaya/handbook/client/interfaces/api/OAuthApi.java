package dev.sayaya.handbook.client.interfaces.api;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OAuthApi {
    private final FetchApi fetchApi;
    @Inject OAuthApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }
}
