package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.DocumentRepository;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static elemental2.core.Global.JSON;

@Singleton
public class DocumentApi implements SearchApi<DocumentNative>, DocumentRepository {
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
                .sortBy("serial").asc(true).limit(999999)
                .build();
        return search("workspace/" + workspace.id() + "/documents", param)
                        .then(this::parse)
                        .finally_(()-> progress.next(Progress.builder().enabled(false).build()))
                        .catch_(this::handleException);
    }
    public void reload() {
        search().then(page->{
            documents.set(page.getContent());
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

    @Override
    public Observable<Void> save(Set<Document> toUpsert) {
        return persist(toUpsert, "PUT");
    }
    @Override
    public Observable<Void> delete(Set<Document> toDelete) {
        return persist(toDelete, "DELETE");
    }
    private Observable<Void> persist(Set<Document> documents, String method) {
        if(workspace==null) return Observable.of((Void)null);
        progress.next(Progress.builder().enabled(true).intermediate(true).build());
        var request = RequestInit.create();
        request.setMethod(method);
        request.setHeaders(new String[][] {
                new String[] {"Content-Type", "application/vnd.sayaya.handbook.v1+json"}
        });
        var natives = documents.stream().map(DocumentNative::from).toArray(DocumentNative[]::new);
        request.setBody(JSON.stringify(natives));
        return AsyncSubject.await(fetchApi
                .request("workspace/" + workspace.id() + "/documents", request)
                .then(this::handleResponse)
                .then(resp -> Promise.resolve((Void)null))
                .finally_(()-> progress.next(Progress.builder().enabled(false).build()))
                .catch_(this::handleException)
        );
    }
    private Promise<Response> handleResponse(Response response) {
        return switch (response.status) {
            case 200 -> Promise.resolve(response);
            case 204 -> Promise.reject("Empty result");
            default  -> Promise.reject("HTTP Error: " + response.status + " - " + response.statusText);
        };
    }
}
