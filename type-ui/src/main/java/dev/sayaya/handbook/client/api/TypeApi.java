package dev.sayaya.handbook.client.api;

import dev.sayaya.handbook.client.domain.Page;
import dev.sayaya.handbook.client.domain.Search;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
public class TypeApi implements SearchApi<TypeNative>, TypeRepository {
    private final FetchApi fetchApi;
    @Inject TypeApi(FetchApi fetchApi) {
        this.fetchApi = fetchApi;
    }

    @Override
    public Promise<Response> searchRequest(String url) {
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        return fetchApi.request(url, request);
    }
    @Override
    public Observable<Page<Type>> search(Search search) {
        // if(JsWindow.progress!=null) JsWindow.progress.enabled(true).intermediate(true);
        var promise = search("types", search);/*.finally_(()-> {
             if(JsWindow.progress!=null) JsWindow.progress.enabled(false);
        })*/;
        return AsyncSubject.await(promise).map(page-> {
            var natives = page.content();
            var domain = Arrays.stream(natives).map(TypeNative::toType).toArray(Type[]::new);
            return Page.<Type>builder().totalPages(page.totalPages()).totalElements(page.totalElements()).content(domain).build();
        });
    }
}
