package dev.sayaya.handbook.client.interfaces.create;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode.Mode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;
import dev.sayaya.ui.elements.RadioElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

/**
 * 워크스페이스 생성 섹션. MD3 Radio 버튼 + 라벨 + 입력 필드.
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link RadioElementBuilder} — MD3 Radio (sayaya-ui)</li>
 *   <li>{@link TextFieldElementBuilder} — MD3 TextField (sayaya-ui)</li>
 * </ul></p>
 */
public class SectionElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final RadioElementBuilder radio;
    private final HTMLElement label;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder input;
    private final Mode mode;

    @AssistedInject
    SectionElement(@Assisted Mode mode, CreateWorkspaceMode modeState, CreateWorkspaceParam param) {
        this.mode = mode;

        radio = RadioElementBuilder.radio()
                .name("create-workspace")
                .value(mode.name());

        label = (HTMLElement) DomGlobal.document.createElement("label");
        label.classList.add("ws-section-label");

        input = TextFieldElementBuilder.textField().outlined().css("ws-section-input");

        radio.onChange((e) -> {
            modeState.next(mode);
            input.element().focus();
        });
        input.element().addEventListener("focus", e -> modeState.next(mode));
        input.element().addEventListener("input", e -> {
            if (modeState.getValue() == mode) param.next(input.element().value);
        });

        modeState.subscribe(m -> {
            radio.select(m == mode);
            if (m != mode) {
                input.element().value = "";
                param.next(null);
            }
        });

        root = div().css("ws-section").element();
        root.appendChild(radio.element());
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
