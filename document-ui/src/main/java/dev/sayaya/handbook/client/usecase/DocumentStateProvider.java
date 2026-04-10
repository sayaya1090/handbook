package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.usecase.StateProvider;
import elemental2.core.Global;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 현재 문서 상태를 JSON 스냅샷으로 제공하는 StateProvider 구현체.
 *
 * <p><b>책임:</b> {@link DocumentList}와 {@link TypeProvider}로부터 현재 문서 목록과
 * 선택된 타입을 조회하여 JSON 문자열로 직렬화한다. 에이전트가 현재 UI 상태를
 * 파악하는 데 사용된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentList} — 현재 문서 목록 조회</li>
 *   <li>{@link TypeProvider} — 현재 선택된 타입 조회</li>
 *   <li>{@link dev.sayaya.handbook.usecase.StateProvider} — 구현 대상 포트 인터페이스</li>
 * </ul></p>
 *
 * <p><b>주의:</b> Global.JSON.stringify로 직렬화하므로 순환 참조가 있으면 오류가 발생한다.</p>
 */
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
