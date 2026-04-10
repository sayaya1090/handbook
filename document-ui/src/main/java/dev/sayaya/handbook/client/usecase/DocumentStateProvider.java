package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.usecase.StateProvider;
import elemental2.core.Global;

import javax.inject.Inject;
import javax.inject.Singleton;

/** 현재 문서 목록을 JSON 스냅샷으로 제공한다. */
@Singleton
public class DocumentStateProvider implements StateProvider {
    private final DocumentList documentList;
    private final TypeProvider typeProvider;

    @Inject
    public DocumentStateProvider(DocumentList documentList, TypeProvider typeProvider) {
        this.documentList = documentList;
        this.typeProvider = typeProvider;
    }

    @Override
    public String snapshot() {
        var docs = documentList.getValue();
        var type = typeProvider.getValue();
        return Global.JSON.stringify(new Object[]{type, docs.toArray()});
    }
}
