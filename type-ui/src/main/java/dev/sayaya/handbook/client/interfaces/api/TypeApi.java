package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Search;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class TypeApi implements SearchApi<TypeNative>, TypeRepository {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    private Workspace workspace;
    @Inject TypeApi(FetchApi fetchApi, Observer<Progress> progress, Observable<Workspace> workspace) {
        this.fetchApi = fetchApi;
        this.progress = progress;
        workspace.distinctUntilChanged().subscribe(w-> this.workspace = w);
    }

    @Override
    public Promise<Response> searchRequest(String url) {
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        return fetchApi.request(url, request);
    }
    public Observable<List<Type>> list() {
        if(workspace==null) return Observable.of(List.of());
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var promise = search("workspaces/" + workspace.id() + "/types", Search.builder().limit(100).build()).finally_(()-> progress.next(Progress.builder().enabled(false).build()));
        return AsyncSubject.await(promise).map(page-> {
            var natives = page.content();
            return Arrays.stream(natives).map(TypeNative::toType).collect(Collectors.toList());
        });
    }
}
