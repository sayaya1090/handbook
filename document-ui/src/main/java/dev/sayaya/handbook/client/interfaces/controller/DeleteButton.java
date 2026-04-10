package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.DeleteDocumentAction;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.usecase.LabelProvider;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.button;

/**
 * 선택 문서 삭제 버튼.
 *
 * <p><b>책임:</b> 클릭 시 문서 목록의 마지막 항목을 {@link DeleteDocumentAction}으로 제거한다.
 * ActionManager를 경유하므로 Undo/Redo가 지원된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ActionManager} — 액션 실행 및 Undo/Redo 스택 관리</li>
 *   <li>{@link DocumentList} — 삭제 대상 문서 목록 조회</li>
 *   <li>{@link dev.sayaya.handbook.usecase.LabelProvider} — 버튼 레이블 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 현재 구현은 항상 목록의 마지막 문서를 삭제한다.
 * 목록이 비어 있으면 아무 동작도 하지 않는다.</p>
 */
@Singleton
public class DeleteButton implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLElement element;

    @Inject
    public DeleteButton(ActionManager actionManager, DocumentList documentList, LabelProvider labelProvider) {
        this.element = button().css("doc-ctrl-btn", "doc-ctrl-btn-delete").element();
        labelProvider.subscribe(labels -> element.textContent = labels.getOrDefault("document.delete", "Delete"));
        element.addEventListener("click", e -> {
            var docs = documentList.getValue();
            if (!docs.isEmpty()) {
                int lastIdx = docs.size() - 1;
                actionManager.execute(new DeleteDocumentAction(
                    documentList,
                    List.of(docs.get(lastIdx)),
                    List.of(lastIdx)
                ));
            }
        });
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
