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
 * 범용 오버레이 컨테이너.
 * coachmark, spotlight, pulse, arrow, badge 5가지 스타일을 지원한다.
 * 온보딩, 정합성 경고, 협업 공유, 에이전트 안내 등에 공통으로 사용한다.
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

    private native void positionNear(Element tooltip, Element target, String position) /*-{
        var rect = target.getBoundingClientRect();
        tooltip.style.position = 'fixed';
        switch(position) {
            case 'top':
                tooltip.style.left = (rect.left + rect.width / 2) + 'px';
                tooltip.style.top = rect.top + 'px';
                tooltip.style.transform = 'translate(-50%, -100%)';
                break;
            case 'bottom':
                tooltip.style.left = (rect.left + rect.width / 2) + 'px';
                tooltip.style.top = rect.bottom + 'px';
                tooltip.style.transform = 'translate(-50%, 8px)';
                break;
            case 'left':
                tooltip.style.left = rect.left + 'px';
                tooltip.style.top = (rect.top + rect.height / 2) + 'px';
                tooltip.style.transform = 'translate(-100%, -50%)';
                break;
            case 'right':
                tooltip.style.left = rect.right + 'px';
                tooltip.style.top = (rect.top + rect.height / 2) + 'px';
                tooltip.style.transform = 'translate(8px, -50%)';
                break;
        }
    }-*/;

    @Override
    public HTMLDivElement element() { return root; }
}
