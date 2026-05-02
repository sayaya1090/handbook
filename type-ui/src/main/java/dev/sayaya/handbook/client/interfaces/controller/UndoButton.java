package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.interfaces.api.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 마지막 액션을 되돌리는 Undo Text 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link ActionManager#undo()}를 호출하고,
 * undo 가능 여부에 따라 disabled 속성을 자동 토글한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ActionManager} — undo 실행 및 canUndo 구독</li>
 *   <li>{@link LabelProvider} — 다국어 툴팁</li>
 * </ul></p>
 * <p><b>주의:</b> 키보드 단축키(Ctrl+Z)는 {@link dev.sayaya.handbook.client.interfaces.canvas.CanvasElement}에서 별도 처리한다.</p>
 */
@Singleton
public class UndoButton implements IsElement<HTMLElement> {
    @Delegate private final ButtonElementBuilder.TextButtonElementBuilder _this;

    @Inject
    UndoButton(ActionManager actionManager, LabelProvider labelProvider) {
        _this = ButtonElementBuilder.button().text()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-rotate-left"))
                .css("type-ctrl-btn", "type-ctrl-btn-undo");

        _this.onClick(e -> actionManager.undo());
        actionManager.onCanUndo(can -> _this.disabled(!can));

        labelProvider.subscribe(labels ->
                _this.element().title = labels.getOrDefault("type.undo", "Undo"));
    }
}
