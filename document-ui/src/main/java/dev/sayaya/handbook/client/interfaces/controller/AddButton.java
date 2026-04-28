package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.domain.DocumentValue;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.AddDocumentAction;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.usecase.LabelProvider;
import jsinterop.base.JsPropertyMap;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 새 문서 추가 버튼.
 *
 * <p><b>책임:</b> 클릭 시 빈 {@link DocumentValue}를 생성하고
 * {@link AddDocumentAction}을 통해 {@link DocumentList}에 추가한다.
 * ActionManager를 경유하므로 Undo/Redo가 지원된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ActionManager} — 액션 실행 및 Undo/Redo 스택 관리</li>
 *   <li>{@link DocumentList} — 현재 문서 목록 상태</li>
 *   <li>{@link dev.sayaya.handbook.usecase.LabelProvider} — 버튼 레이블 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 싱글턴으로 관리되며, 레이블은 LabelProvider 구독을 통해 언어 변경 시 자동 갱신된다.</p>
 */
@Singleton
public class AddButton implements IsElement<elemental2.dom.HTMLElement> {
    @Delegate private final ButtonElementBuilder.TextButtonElementBuilder _this;

    @Inject
    public AddButton(ActionManager actionManager, DocumentList documentList, LabelProvider labelProvider) {
        this._this = ButtonElementBuilder.button().text().css("doc-ctrl-btn", "doc-ctrl-btn-add");
        labelProvider.subscribe(labels -> _this.text(labels.getOrDefault("document.add", "Add")));
        _this.onClick(e -> {
            DocumentValue newDoc = new DocumentValue();
            newDoc.data = JsPropertyMap.of();
            actionManager.execute(new AddDocumentAction(documentList, newDoc));
        });
    }
}
