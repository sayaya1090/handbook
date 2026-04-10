package dev.sayaya.handbook.client.components;

import dev.sayaya.handbook.domain.OverlayStyle;
import dev.sayaya.ui.elements.BadgeElementBuilder;
import dev.sayaya.ui.elements.CardElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

/**
 * 범용 오버레이 컨테이너 컴포넌트.
 *
 * <p><b>책임:</b> CSS 선택자 대상 요소에 coachmark/spotlight/pulse/arrow/badge 5가지 스타일의 오버레이를 렌더링한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link OverlayStyle} — 오버레이 스타일 열거형</li>
 *   <li>{@link CardElementBuilder} — 툴팁/메시지 카드 렌더링</li>
 *   <li>{@link BadgeElementBuilder} — 뱃지 스타일 렌더링</li>
 * </ul></p>
 * <p><b>주의:</b> positionNear()는 getBoundingClientRect()로 fixed 위치를 계산한다.</p>
 */
public class OverlayContainer implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final EventListener dismissListener = e -> hide();
    private boolean listenerAttached = false;

    public OverlayContainer() {
        root = div().css("ui-overlay-container").element();
        root.style.set("display", "none");
    }

    /** 오버레이를 표시한다. */
    public void show(String targetSelector, OverlayStyle style, String message, String position, boolean dismissable) {
        root.innerHTML = "";
        root.style.set("display", "block");

        Element target = DomGlobal.document.querySelector(targetSelector);
        if (target == null) return;

        switch (style) {
            case COACHMARK: renderCoachmark(target, message, position); break;
            case SPOTLIGHT: renderSpotlight(target, message, position); break;
            case PULSE:
                target.classList.add("ui-pulse");
                renderTooltipCard(target, message, position);
                break;
            case ARROW: renderArrow(target, message, position); break;
            case BADGE: renderBadge(target, message); break;
        }

        if (listenerAttached) { root.removeEventListener("click", dismissListener); listenerAttached = false; }
        if (dismissable) { root.addEventListener("click", dismissListener); listenerAttached = true; }
    }

    /** 오버레이를 숨긴다. */
    public void hide() {
        root.style.set("display", "none");
        root.innerHTML = "";
        Element s = DomGlobal.document.querySelector(".ui-spotlight-target");
        if (s != null) s.classList.remove("ui-spotlight-target");
        Element p = DomGlobal.document.querySelector(".ui-pulse");
        if (p != null) p.classList.remove("ui-pulse");
    }

    private void renderCoachmark(Element target, String message, String position) {
        root.appendChild(div().css("ui-coachmark-backdrop").element());
        HTMLElement tooltip = CardElementBuilder.card().elevated().css("ui-coachmark-tooltip", "ui-position-" + position).element();
        HTMLDivElement content = div().element();
        content.textContent = message;
        tooltip.appendChild(content);
        positionNear(tooltip, target, position);
        root.appendChild(tooltip);
    }

    private void renderSpotlight(Element target, String message, String position) {
        root.appendChild(div().css("ui-spotlight-backdrop").element());
        target.classList.add("ui-spotlight-target");
        HTMLElement tooltip = CardElementBuilder.card().elevated().css("ui-spotlight-message", "ui-position-" + position).element();
        HTMLDivElement content = div().element();
        content.textContent = message;
        tooltip.appendChild(content);
        positionNear(tooltip, target, position);
        root.appendChild(tooltip);
    }

    private void renderTooltipCard(Element target, String message, String position) {
        if (message == null || message.isEmpty()) return;
        HTMLElement card = CardElementBuilder.card().elevated().css("ui-tooltip-card", "ui-position-" + position).element();
        HTMLDivElement content = div().element();
        content.textContent = message;
        card.appendChild(content);
        positionNear(card, target, position);
        root.appendChild(card);
    }

    private void renderArrow(Element target, String message, String position) {
        HTMLDivElement arrow = div().css("ui-arrow", "ui-position-" + position).element();
        arrow.textContent = "\u25BC";
        HTMLElement card = CardElementBuilder.card().elevated().css("ui-arrow-message").element();
        HTMLDivElement content = div().element();
        content.textContent = message;
        card.appendChild(content);
        positionNear(arrow, target, position);
        positionNear(card, target, position);
        root.appendChild(arrow);
        root.appendChild(card);
    }

    private void renderBadge(Element target, String message) {
        HTMLElement badge = BadgeElementBuilder.badge().value(message).css("ui-badge").element();
        ((HTMLElement) target).style.set("position", "relative");
        target.appendChild(badge);
    }

    /**
     * tooltip 요소를 target 요소 근처에 fixed 위치로 배치한다.
     *
     * @param tooltip 배치할 요소
     * @param target 기준 요소
     * @param position "top", "bottom", "left", "right" 중 하나
     */
    private void positionNear(Element tooltip, Element target, String position) {
        elemental2.dom.DOMRect rect = target.getBoundingClientRect();
        elemental2.dom.CSSStyleDeclaration style = ((HTMLElement) tooltip).style;
        style.setProperty("position", "fixed");
        switch (position) {
            case "top":
                style.setProperty("left", (rect.left + rect.width / 2) + "px");
                style.setProperty("top", rect.top + "px");
                style.setProperty("transform", "translate(-50%, -100%)");
                break;
            case "bottom":
                style.setProperty("left", (rect.left + rect.width / 2) + "px");
                style.setProperty("top", rect.bottom + "px");
                style.setProperty("transform", "translate(-50%, 8px)");
                break;
            case "left":
                style.setProperty("left", rect.left + "px");
                style.setProperty("top", (rect.top + rect.height / 2) + "px");
                style.setProperty("transform", "translate(-100%, -50%)");
                break;
            case "right":
                style.setProperty("left", rect.right + "px");
                style.setProperty("top", (rect.top + rect.height / 2) + "px");
                style.setProperty("transform", "translate(8px, -50%)");
                break;
            default:
                break;
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}
