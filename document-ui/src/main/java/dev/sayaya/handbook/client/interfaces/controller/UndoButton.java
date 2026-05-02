package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Undo 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link ActionManager#undo()}를 호출하여 마지막 액션을 되돌린다.
 * Undo 가능 여부에 따라 버튼의 disabled 상태가 자동으로 토글된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ActionManager} — Undo 실행 및 canUndo 상태 구독</li>
 *   <li>{@link dev.sayaya.handbook.usecase.LabelProvider} — 버튼 레이블 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> ActionManager.onCanUndo 콜백으로 disabled 속성을 제어하므로,
 * 액션 스택이 비어 있으면 버튼이 비활성화된다.</p>
 */
@Singleton
public class UndoButton implements IsElement<elemental2.dom.HTMLElement> {
    @Delegate private final ButtonElementBuilder.TextButtonElementBuilder _this;

    @Inject
    public UndoButton(ActionManager actionManager, LabelProvider labelProvider) {
        this._this = ButtonElementBuilder.button().text().css("doc-ctrl-btn", "doc-ctrl-btn-undo");
        labelProvider.subscribe(labels -> _this.text(labels.getOrDefault("document.undo", "Undo")));
        _this.onClick(e->actionManager.undo());
        actionManager.onCanUndo(can -> {
            _this.disabled(!can);
        });
    }
}
