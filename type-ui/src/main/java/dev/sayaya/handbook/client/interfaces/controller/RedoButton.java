package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 되돌린 액션을 다시 실행하는 Redo Text 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link ActionManager#redo()}를 호출하고,
 * redo 가능 여부에 따라 disabled 속성을 자동 토글한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ActionManager} — redo 실행 및 canRedo 구독</li>
 *   <li>{@link LabelProvider} — 다국어 툴팁</li>
 * </ul></p>
 * <p><b>주의:</b> 키보드 단축키(Ctrl+Shift+Z)는 {@link dev.sayaya.handbook.client.interfaces.canvas.CanvasElement}에서 별도 처리한다.</p>
 */
@Singleton
public class RedoButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    RedoButton(ActionManager actionManager, LabelProvider labelProvider) {
        root = ButtonElementBuilder.button().text()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-rotate-right"))
                .css("type-ctrl-btn").css("type-ctrl-btn-redo")
                .element();

        root.addEventListener("click", e -> actionManager.redo());
        actionManager.onCanRedo(can -> root.toggleAttribute("disabled", !can));

        labelProvider.subscribe(labels ->
                root.title = labels.getOrDefault("type.redo", "Redo"));
    }

    @Override
    public HTMLElement element() { return root; }
}
