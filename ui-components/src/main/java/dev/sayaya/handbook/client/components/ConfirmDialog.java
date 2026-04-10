package dev.sayaya.handbook.client.components;

import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;

/**
 * 범용 확인 다이얼로그 컴포넌트.
 *
 * <p><b>책임:</b> headline과 옵션 버튼 배열을 MD3 Dialog로 표시하고, 사용자 선택을 Consumer 콜백으로 반환한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link DialogElementBuilder} — MD3 다이얼로그 렌더링</li>
 *   <li>{@link ButtonElementBuilder} — 옵션 버튼 생성</li>
 * </ul></p>
 * <p><b>주의:</b> 삭제 확인, 벌크 작업 승인, 에이전트 확인 등에 공통으로 사용한다.</p>
 */
public class ConfirmDialog implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    public ConfirmDialog() {
        root = div().css("ui-confirm-wrapper").element();
    }

    /** 확인 다이얼로그를 표시한다. 사용자가 옵션을 선택하면 onSelect 콜백이 호출된다. */
    public void show(String headline, String[] options, Consumer<String> onSelect) {
        root.innerHTML = "";

        HTMLDivElement actionsContainer = div().css("ui-confirm-actions").element();
        DialogElementBuilder dialogBuilder = DialogElementBuilder.dialog()
                .css("ui-confirm-dialog")
                .headline(headline)
                .actions(actionsContainer);

        for (String option : options) {
            HTMLElement btn = ButtonElementBuilder.button().text()
                    .text(option)
                    .css("ui-confirm-option")
                    .element();
            btn.addEventListener("click", e -> {
                dialogBuilder.open(false);
                if (onSelect != null) onSelect.accept(option);
            });
            actionsContainer.appendChild(btn);
        }

        root.appendChild(dialogBuilder.element());
        dialogBuilder.open(true);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
