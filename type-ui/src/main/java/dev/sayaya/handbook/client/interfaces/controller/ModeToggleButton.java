package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.CanvasMode;
import dev.sayaya.handbook.client.usecase.CanvasMode.Mode;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * LAYOUT / TYPE 모드 전환 토글 버튼 그룹.
 *
 * <p><b>책임:</b> 두 개의 Outlined 버튼으로 캔버스 모드를 전환한다.
 * LAYOUT 모드에서는 박스 이동/리사이즈가, TYPE 모드에서는 인라인 편집이 활성화된다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link CanvasMode} — 현재 모드 상태 관리</li>
 *   <li>{@link LabelProvider} — 다국어 툴팁</li>
 * </ul></p>
 * <p><b>주의:</b> selected 속성으로 현재 활성 모드를 시각적으로 표시한다.
 * LAYOUT 아이콘: arrows-move, TYPE 아이콘: pen.</p>
 */
@Singleton
public class ModeToggleButton implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLElement layoutBtn;
    private final HTMLElement typeBtn;

    @Inject
    ModeToggleButton(CanvasMode canvasMode, LabelProvider labelProvider) {
        layoutBtn = ButtonElementBuilder.button().outlined()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-arrows-up-down-left-right"))
                .css("type-ctrl-btn")
                .element();

        typeBtn = ButtonElementBuilder.button().outlined()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-pen"))
                .css("type-ctrl-btn")
                .element();

        layoutBtn.addEventListener("click", e -> canvasMode.setMode(Mode.LAYOUT));
        typeBtn.addEventListener("click", e -> canvasMode.setMode(Mode.TYPE));

        canvasMode.subscribe(mode -> {
            layoutBtn.toggleAttribute("selected", mode == Mode.LAYOUT);
            typeBtn.toggleAttribute("selected", mode == Mode.TYPE);
        });

        labelProvider.subscribe(labels -> {
            layoutBtn.title = labels.getOrDefault("type.mode.layout", "Layout Mode");
            typeBtn.title = labels.getOrDefault("type.mode.type", "Type Mode");
        });

        root = div().css("type-ctrl-group")
                .add(layoutBtn)
                .add(typeBtn)
                .element();
    }

    @Override
    public HTMLDivElement element() { return root; }
}
