package dev.sayaya.handbook.client.interfaces.create;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode.Mode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

/**
 * 워크스페이스 생성 섹션. 라디오 버튼 + 라벨 + 입력 필드.
 */
public class SectionElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLInputElement radio;
    private final HTMLElement label;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder input;
    private final Mode mode;

    @AssistedInject
    SectionElement(@Assisted Mode mode, CreateWorkspaceMode modeState, CreateWorkspaceParam param) {
        this.mode = mode;

        radio = (HTMLInputElement) DomGlobal.document.createElement("input");
        radio.type = "radio";
        radio.name = "create-workspace";
        radio.value = mode.name();

        label = (HTMLElement) DomGlobal.document.createElement("label");
        label.classList.add("ws-section-label");

        input = TextFieldElementBuilder.textField().outlined().css("ws-section-input");

        radio.addEventListener("change", e -> {
            modeState.next(mode);
            input.element().focus();
        });
        input.element().addEventListener("focus", e -> modeState.next(mode));
        input.element().addEventListener("input", e -> {
            if (modeState.getValue() == mode) param.next(input.element().value);
        });

        modeState.subscribe(m -> {
            radio.checked = (m == mode);
            if (m != mode) {
                input.element().value = "";
                param.next(null);
            }
        });

        root = div().css("ws-section").element();
        root.appendChild(radio);
        HTMLDivElement content = div().css("ws-section-content").element();
        content.appendChild(label);
        content.appendChild(input.element());
        root.appendChild(content);
    }

    public SectionElement label(String text) {
        label.textContent = text;
        return this;
    }

    public SectionElement placeholder(String text) {
        input.label(text);
        return this;
    }

    @Override
    public HTMLDivElement element() { return root; }
}
