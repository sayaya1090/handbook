package dev.sayaya.handbook.client.onboarding;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.ui.elements.RadioElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.Elements;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

/**
 * 워크스페이스 생성/참여 섹션. MD3 Radio + 라벨 + 보조 설명 + 입력 필드로 구성된 카드형 요소.
 *
 * <p><b>구성:</b>
 * <ul>
 *   <li>{@code .ws-section} — 카드 컨테이너 (선택 시 elevated + accent border)</li>
 *   <li>{@code .ws-section-radio} — MD3 Radio 버튼 (좌측 상단 정렬)</li>
 *   <li>{@code .ws-section-label} — title-medium 타이포 헤드라인</li>
 *   <li>{@code .ws-section-supporting} — body-medium 타이포 보조 설명</li>
 *   <li>{@code .ws-section-input} — outlined text field</li>
 * </ul></p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link RadioElementBuilder} — MD3 Radio (sayaya-ui)</li>
 *   <li>{@link TextFieldElementBuilder} — MD3 TextField (sayaya-ui)</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 기존 테스트가 `.ws-section`, `.ws-section-input` selector 를 사용한다.
 * 클래스명 변경 시 회귀 발생.</p>
 */
/**
 * 워크스페이스 생성/참여 섹션. MD3 Radio + 라벨 + 보조 설명 + 입력 필드로 구성된 카드형 요소.
 *
 * <p><b>구성:</b>
 * <ul>
 *   <li>{@code .ws-section} — 카드 컨테이너</li>
 *   <li>{@code .ws-section-radio} — MD3 Radio 버튼</li>
 *   <li>{@code .ws-section-label} — title-medium 타이포 헤드라인</li>
 *   <li>{@code .ws-section-supporting} — body-medium 타이포 보조 설명</li>
 *   <li>{@code .ws-section-input} — outlined text field</li>
 * </ul></p>
 *
 * <p><b>Dumb View 책임:</b> 레이아웃 구성 및 뷰 인터페이스 노출.
 * 상태 구독 및 비즈니스 로직은 {@link SectionElementPresenter} 로 위임됨.</p>
 *
 * <p><b>주의:</b> 기존 테스트가 `.ws-section`, `.ws-section-input` selector 를 사용하므로 CSS 클래스명은 절대 변경 금지.</p>
 */
public class SectionElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final RadioElementBuilder radio;
    private final HTMLElement label;
    private final HTMLElement supporting;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder input;

    @AssistedInject
    SectionElement(@Assisted String modeName) {
        radio = RadioElementBuilder.radio()
                .name("create-workspace")
                .value(modeName);
        radio.element().classList.add("ws-section-radio");

        label = Elements.label().css("ws-section-label").element();
        supporting = Elements.span().css("ws-section-supporting").element();
        input = TextFieldElementBuilder.textField().outlined().css("ws-section-input");

        HTMLDivElement content = div().css("ws-section-content").element();
        content.appendChild(label);
        content.appendChild(supporting);
        content.appendChild(input.element());

        root = div().css("ws-section").element();
        root.appendChild(radio.element());
        root.appendChild(content);
    }

    public void onInputFocus(Runnable action) { input.on(EventType.focus, e -> action.run()); }
    public void onInputChanged(java.util.function.Consumer<String> action) { input.on(EventType.input, e -> action.accept(input.element().value)); }
    public void onRadioChanged(Runnable action) { radio.onChange(e -> action.run()); }

    public void setActive(boolean active) {
        if (active) root.classList.add("ws-section-active");
        else root.classList.remove("ws-section-active");
        radio.select(active);
    }

    public void clearInput() { input.element().value = ""; }
    public void focusInput() { input.element().focus(); }

    public SectionElement label(String text) { label.textContent = text; return this; }
    public SectionElement supportingText(String text) { supporting.textContent = text; return this; }
    public SectionElement placeholder(String text) { input.label(text); return this; }

    @Override
    public HTMLDivElement element() { return root; }
    
    @dagger.assisted.AssistedFactory
    public interface Factory {
        SectionElement create(String modeName);
    }
}
