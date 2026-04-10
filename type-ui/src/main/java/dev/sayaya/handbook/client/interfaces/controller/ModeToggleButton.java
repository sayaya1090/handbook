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
 * LAYOUT / TYPE 모드 전환 토글.
 * LAYOUT: 이동/리사이즈 (아이콘: arrows-move)
 * TYPE: 인라인 편집 (아이콘: pen)
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
