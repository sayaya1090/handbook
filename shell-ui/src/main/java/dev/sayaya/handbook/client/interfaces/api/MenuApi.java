package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.rx.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class MenuApi implements MenuRepository {
    private final FetchApi fetchApi;
    @Inject MenuApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }
    @Override
    public Observable<List<Menu>> findAll() {
        return null;
    }
}
