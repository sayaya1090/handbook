package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.stream.Collectors;

@Singleton
public class DocumentApi implements SearchApi<DocumentNative> {
    private final FetchApi fetchApi;
    private final Observer<Progress> progress;
    private final TypeProvider type;
    private final DocumentList documents;
    private Workspace workspace;
    @Inject DocumentApi(FetchApi fetchApi, Observer<Progress> progress, Observable<Workspace> workspace, TypeProvider type, DocumentList documents) {
        this.fetchApi = fetchApi;
        this.progress = progress;
        this.type = type;
        this.documents = documents;
        workspace.distinctUntilChanged().subscribe(w-> this.workspace = w);
        type.subscribe(t->reload());
    }
    @Override
    public Promise<Response> searchRequest(String url) {
        var request = RequestInit.create();
        request.setHeaders(new String[][] {
                new String[] {"Accept", "application/vnd.sayaya.handbook.v1+json"}
        });
        return fetchApi.request(url, request);
    }
    private <V> V handleException(Object throwable) {
        throw new RuntimeException("Request failed: " + throwable);
    }
    public Promise<Page<Document>> search() {
        if(workspace==null) return Promise.resolve(Page.<Document>builder().content(new Document[0]).build());
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var param = Search.builder()
                .filter("type", type.getValue().id())
                .filter("effect_date_time", String.valueOf(type.getValue().effectDateTime().getTime()))
                .filter("expire_date_time", String.valueOf(type.getValue().expireDateTime().getTime()))
                .build();
        return search("workspace/" + workspace.id() + "/documents", param)
                        .then(this::parse)
                        .finally_(()-> progress.next(Progress.builder().enabled(false).build()))
                        .catch_(this::handleException);
    }
    public void reload() {
        search().then(page->{
            documents.next(Arrays.stream(page.getContent()).collect(Collectors.toUnmodifiableList()));
            return null;
        });
    }
    private Promise<Page<Document>> parse(Page<DocumentNative> response) {
        var page = Page.<Document>builder()
                .totalElements(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .content(Arrays.stream(response.getContent()).map(DocumentNative::toDomain).toArray(Document[]::new))
                .build();
        return Promise.resolve(page);
    }
}
