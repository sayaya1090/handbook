package dev.sayaya.handbook.client.interfaces.create;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode.Mode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;
import dev.sayaya.ui.elements.RadioElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import org.jboss.elemento.Elements;

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
 *   <li>{@link CreateWorkspaceMode} — CREATE/JOIN 모드 상태</li>
 *   <li>{@link CreateWorkspaceParam} — 입력값 스트림</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 기존 테스트가 `.ws-section`, `.ws-section-input` selector 를 사용한다.
 * 클래스명 변경 시 회귀 발생.</p>
 */
public class SectionElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final RadioElementBuilder radio;
    private final HTMLElement label;
    private final HTMLElement supporting;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder input;
    private final Mode mode;

    @AssistedInject
    SectionElement(@Assisted Mode mode, CreateWorkspaceMode modeState, CreateWorkspaceParam param) {
        this.mode = mode;

        radio = RadioElementBuilder.radio()
                .name("create-workspace")
                .value(mode.name());
        radio.element().classList.add("ws-section-radio");

        label = Elements.label().css("ws-section-label").element();
        supporting = Elements.span().css("ws-section-supporting").element();

        input = TextFieldElementBuilder.textField().outlined().css("ws-section-input");
        input.on(EventType.focus, e -> modeState.next(mode))
                .on(EventType.input, e -> {
                    if (modeState.getValue() == mode) param.next(input.element().value);
                });

        radio.onChange(e -> {
            modeState.next(mode);
            input.element().focus();
        });

        HTMLDivElement content = div().css("ws-section-content").element();
        content.appendChild(label);
        content.appendChild(supporting);
        content.appendChild(input.element());

        // root 초기화를 modeState.subscribe 보다 먼저 수행 — BehaviorSubject 가 즉시 emit 하므로
        // subscribe 콜백이 동기로 실행되어 root.classList 접근이 NPE 나지 않도록.
        root = div().css("ws-section").element();
        root.appendChild(radio.element());
        root.appendChild(content);

        modeState.subscribe(m -> {
            boolean active = (m == mode);
            radio.select(active);
            if (active) root.classList.add("ws-section-active");
            else {
                root.classList.remove("ws-section-active");
                input.element().value = "";
                param.next(null);
            }
        });
    }

    public SectionElement label(String text) {
        label.textContent = text;
        return this;
    }

    public SectionElement supportingText(String text) {
        supporting.textContent = text;
        return this;
    }

    public SectionElement placeholder(String text) {
        input.label(text);
        return this;
    }

    @Override
    public HTMLDivElement element() { return root; }
}
