package dev.sayaya.handbook.client.components;

import dev.sayaya.ui.elements.CardElementBuilder;
import dev.sayaya.ui.elements.DividerElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

/**
 * 범용 변경사항 Diff 패널.
 * "before → after" 형식의 변경 내역을 MD3 카드로 표시한다.
 * 버전 비교, 변경 이력, 에이전트 미리보기 등에 공통으로 사용한다.
 */
public class DiffPanel implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLElement card;
    private final HTMLDivElement content;

    public DiffPanel() {
        content = div().css("ui-diff-content").element();
        card = CardElementBuilder.card().outlined().css("ui-diff-panel").element();

        HTMLDivElement header = div().css("ui-diff-header")
                .add(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-code-compare"))
                .element();
        HTMLDivElement headerText = div().element();
        headerText.textContent = "변경 사항 미리보기";
        header.appendChild(headerText);

        card.appendChild(header);
        card.appendChild(DividerElementBuilder.divider().element());
        card.appendChild(content);

        root = div().add(card).element();
        root.style.set("display", "none");
    }

    /** 헤더 텍스트를 변경한다. */
    public void setHeaderText(String text) {
        HTMLDivElement header = (HTMLDivElement) card.querySelector(".ui-diff-header div:last-child");
        if (header != null) header.textContent = text;
    }

    /** 변경 내역을 표시한다. "→" 포함 시 before/after 스타일 적용. */
    public void show(String[] changes) {
        content.innerHTML = "";
        root.style.set("display", "block");

        for (String change : changes) {
            HTMLDivElement line = div().css("ui-diff-line").element();
            if (change.contains("→")) {
                String[] parts = change.split("→", 2);
                HTMLDivElement before = div().css("ui-diff-before").element();
                before.textContent = parts[0].trim();

                HTMLElement arrow = IconElementBuilder.icon()
                        .css("fa-sharp", "fa-light", "fa-arrow-right")
                        .element();
                arrow.classList.add("ui-diff-arrow");

                HTMLDivElement after = div().css("ui-diff-after").element();
                after.textContent = parts.length > 1 ? parts[1].trim() : "";

                line.appendChild(before);
                line.appendChild(arrow);
                line.appendChild(after);
            } else {
                line.textContent = change;
            }
            content.appendChild(line);
        }
    }

    /** 패널을 숨긴다. */
    public void hide() {
        root.style.set("display", "none");
        content.innerHTML = "";
    }

    @Override
    public HTMLDivElement element() { return root; }
}
