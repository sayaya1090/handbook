package dev.sayaya.handbook.client.components;

import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.NodeList;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;

/**
 * 범용 확인 다이얼로그 컴포넌트.
 *
 * <p><b>책임:</b> headline과 옵션 버튼 배열을 MD3 Dialog로 표시하고, 사용자 선택을 Consumer 콜백으로 반환한다.
 * ARIA role="alertdialog", aria-labelledby, Escape 키 닫기, 키보드 트랩을 지원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link DialogElementBuilder} — MD3 다이얼로그 렌더링</li>
 *   <li>{@link ButtonElementBuilder} — 옵션 버튼 생성</li>
 * </ul></p>
 * <p><b>주의:</b> 삭제 확인, 벌크 작업 승인, 에이전트 확인 등에 공통으로 사용한다.
 * 다이얼로그가 열리면 첫 번째 버튼에 포커스가 이동하고, Tab 키로 버튼 간 순환한다.</p>
 */
public class ConfirmDialog implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private static int idCounter = 0;

    public ConfirmDialog() {
        root = div().css("ui-confirm-wrapper").element();
    }

    /** 확인 다이얼로그를 표시한다. 사용자가 옵션을 선택하면 onSelect 콜백이 호출된다. */
    public void show(String headline, String[] options, Consumer<String> onSelect) {
        root.innerHTML = "";
        String headlineId = "ui-confirm-headline-" + (++idCounter);

        HTMLDivElement actionsContainer = div().css("ui-confirm-actions").element();
        DialogElementBuilder dialogBuilder = DialogElementBuilder.dialog()
                .css("ui-confirm-dialog")
                .headline(headline)
                .actions(actionsContainer);

        for (String option : options) {
            // sayaya-ui ButtonElementBuilder 체인에 click 핸들러를 on() 으로 직접 바인딩.
            HTMLElement btn = ButtonElementBuilder.button().text()
                    .text(option)
                    .css("ui-confirm-option")
                    .on(EventType.click, e -> {
                        dialogBuilder.open(false);
                        if (onSelect != null) onSelect.accept(option);
                    })
                    .element();
            actionsContainer.appendChild(btn);
        }

        HTMLElement dialogEl = dialogBuilder.element();
        dialogEl.setAttribute("role", "alertdialog");
        dialogEl.setAttribute("aria-labelledby", headlineId);

        // headline 요소에 id 부여 (MD3 Dialog 내부의 .headline slot)
        NodeList<elemental2.dom.Element> headlines = dialogEl.querySelectorAll("[slot=headline], .headline");
        if (headlines.length > 0) {
            headlines.getAt(0).id = headlineId;
        } else {
            dialogEl.id = headlineId;
        }

        // Escape 키 닫기
        dialogEl.addEventListener("keydown", evt -> {
            KeyboardEvent ke = Js.cast(evt);
            if ("Escape".equals(ke.key)) {
                dialogBuilder.open(false);
            }
        });

        // 키보드 트랩: Tab 키로 버튼 간 순환
        setupKeyboardTrap(actionsContainer, dialogBuilder);

        root.appendChild(dialogEl);
        dialogBuilder.open(true);

        // 열린 후 첫 번째 버튼에 포커스
        DomGlobal.setTimeout(e -> {
            NodeList<elemental2.dom.Element> btns = actionsContainer.querySelectorAll("button, [role=button], md-text-button");
            if (btns.length > 0) Js.<HTMLElement>cast(btns.getAt(0)).focus();
        }, 100);
    }

    private void setupKeyboardTrap(HTMLDivElement container, DialogElementBuilder dialogBuilder) {
        container.addEventListener("keydown", evt -> {
            KeyboardEvent ke = Js.cast(evt);
            if (!"Tab".equals(ke.key)) return;

            NodeList<elemental2.dom.Element> focusable = container.querySelectorAll("button, [role=button], [tabindex], md-text-button");
            if (focusable.length == 0) return;

            HTMLElement first = Js.cast(focusable.getAt(0));
            HTMLElement last = Js.cast(focusable.getAt(focusable.length - 1));
            elemental2.dom.Element active = DomGlobal.document.activeElement;

            if (ke.shiftKey) {
                if (active == first) {
                    ke.preventDefault();
                    last.focus();
                }
            } else {
                if (active == last) {
                    ke.preventDefault();
                    first.focus();
                }
            }
        });
    }

    @Override
    public HTMLDivElement element() { return root; }
}
