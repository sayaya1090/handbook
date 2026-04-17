package dev.sayaya.handbook.client.components;

import dev.sayaya.ui.elements.CardElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

/**
 * MD3 범용 Tooltip 컴포넌트.
 *
 * <p><b>책임:</b> 앵커 요소 근처에 headline(+ supportingText) 텍스트를 floating card
 * 로 표시한다. hover 자동 바인딩 + 외부 트리거 show()/hide() 를 동시 지원해
 * UI 의 일반 tooltip 과 agent-command highlight 동반 표시 양쪽에 사용된다.</p>
 *
 * <p><b>책임 분리:</b> sayaya-ui 라이브러리에 Tooltip 컴포넌트가 없어서
 * {@link OverlayContainer#renderTooltipCard} 에 숨어 있던 CSS(`ui-tooltip-card`,
 * `ui-position-*`) 를 공용 컴포넌트로 추출한 결과. OverlayContainer 는 여전히
 * attention/coachmark 커맨드 처리용으로 유지한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link CardElementBuilder} — elevated Card 레이아웃</li>
 * </ul></p>
 *
 * <p><b>디자인 토큰 (`docs/contracts/design-tokens.md#tooltip`):</b>
 * <ul>
 *   <li>delay: 기본 300ms (MD3 short duration)</li>
 *   <li>elevation: Card elevated (level 2)</li>
 *   <li>position: start / end / top / bottom (기본 end — Navigation Rail 우측)</li>
 * </ul></p>
 *
 * <p><b>주의:</b> MenuRail 처럼 overflow:hidden 내부 요소에 anchor 할 때
 * tooltip 이 잘리지 않도록 card 는 document.body 레벨 fixed 로 배치된다.</p>
 */
public class TooltipCard implements IsElement<HTMLDivElement> {

    public static final int DEFAULT_DELAY_MS = 300;
    public static final String DEFAULT_POSITION = "end";
    public static final int AUTO_HIDE_HIGHLIGHT_MS = 3000;

    private final HTMLDivElement root;
    private final HTMLDivElement headlineEl = div().css("ui-tooltip-headline").element();
    private final HTMLDivElement supportingEl = div().css("ui-tooltip-supporting").element();
    private final Element anchor;

    private String position = DEFAULT_POSITION;
    private int delayMs = DEFAULT_DELAY_MS;
    private boolean enabled = true;
    private double showTimerId = -1;
    private double autoHideTimerId = -1;
    private boolean attached = false;

    private TooltipCard(Element anchor) {
        this.anchor = anchor;
        HTMLElement card = CardElementBuilder.card().elevated()
                .css("ui-tooltip-card", "ui-position-" + position)
                .element();
        card.appendChild(headlineEl);
        card.appendChild(supportingEl);
        supportingEl.style.set("display", "none");
        this.root = div().css("ui-tooltip-portal").element();
        root.style.set("display", "none");
        root.appendChild(card);

        anchor.addEventListener("mouseover", e -> scheduleShow());
        anchor.addEventListener("mouseout", e -> cancelAndHide());
    }

    /**
     * {@link TooltipCard} 를 생성해 anchor 요소 위의 hover 이벤트에 바인딩한다.
     * 반환된 인스턴스의 element() 는 document.body 에 append 해두어야 표시된다.
     */
    public static TooltipCard anchor(Element anchor) {
        return new TooltipCard(anchor);
    }

    /** 표시 방향. "start" | "end" | "top" | "bottom". 기본 "end". */
    public TooltipCard position(String position) {
        this.position = position;
        applyPositionClass();
        return this;
    }

    /** hover intent 지연(ms). 기본 300. 0 이면 즉시. */
    public TooltipCard delay(int ms) {
        this.delayMs = ms;
        return this;
    }

    /** 컨텐츠 설정. supporting 이 null/empty 면 한 줄 짜리 tooltip. */
    public TooltipCard content(String headline, String supporting) {
        headlineEl.textContent = headline != null ? headline : "";
        if (supporting == null || supporting.isEmpty()) {
            supportingEl.textContent = "";
            supportingEl.style.set("display", "none");
        } else {
            supportingEl.textContent = supporting;
            supportingEl.style.set("display", "block");
        }
        return this;
    }

    /**
     * 비활성화 시 hover 에 반응하지 않고 현재 표시 중이면 즉시 숨긴다.
     * MenuRailMode = EXPAND/HIDE 같은 상태에서 호출해 노출 조건을 제한하는 용도.
     */
    public TooltipCard enabled(boolean on) {
        this.enabled = on;
        if (!on) hideImmediate();
        return this;
    }

    /**
     * 외부 트리거용 즉시 표시. autoHideAfterMs 경과 후 자동 hide.
     * highlight agent-command 동반 표시에 사용 (autoHide = AUTO_HIDE_HIGHLIGHT_MS).
     */
    public void showImmediate(int autoHideAfterMs) {
        cancelShowTimer();
        cancelAutoHideTimer();
        doShow();
        if (autoHideAfterMs > 0) {
            autoHideTimerId = DomGlobal.setTimeout(args -> {
                hideImmediate();
                autoHideTimerId = -1;
            }, autoHideAfterMs);
        }
    }

    /** 외부 트리거용 즉시 숨김. */
    public void hideImmediate() {
        cancelShowTimer();
        cancelAutoHideTimer();
        doHide();
    }

    // --- 내부 ---

    private void scheduleShow() {
        if (!enabled) return;
        cancelShowTimer();
        if (delayMs <= 0) {
            doShow();
            return;
        }
        showTimerId = DomGlobal.setTimeout(args -> {
            doShow();
            showTimerId = -1;
        }, delayMs);
    }

    private void cancelAndHide() {
        cancelShowTimer();
        doHide();
    }

    private void cancelShowTimer() {
        if (showTimerId >= 0) {
            DomGlobal.clearTimeout(showTimerId);
            showTimerId = -1;
        }
    }

    private void cancelAutoHideTimer() {
        if (autoHideTimerId >= 0) {
            DomGlobal.clearTimeout(autoHideTimerId);
            autoHideTimerId = -1;
        }
    }

    private void doShow() {
        ensureAttached();
        positionNear();
        root.style.set("display", "block");
    }

    private void doHide() {
        root.style.set("display", "none");
    }

    private void ensureAttached() {
        if (attached) return;
        DomGlobal.document.body.appendChild(root);
        attached = true;
    }

    private void applyPositionClass() {
        HTMLElement card = (HTMLElement) root.firstElementChild;
        if (card == null) return;
        card.classList.remove("ui-position-start", "ui-position-end", "ui-position-top", "ui-position-bottom");
        card.classList.add("ui-position-" + position);
    }

    private void positionNear() {
        elemental2.dom.DOMRect rect = anchor.getBoundingClientRect();
        elemental2.dom.CSSStyleDeclaration style = root.style;
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
            case "start":
                style.setProperty("left", rect.left + "px");
                style.setProperty("top", (rect.top + rect.height / 2) + "px");
                style.setProperty("transform", "translate(-100%, -50%)");
                break;
            case "end":
            default:
                style.setProperty("left", rect.right + "px");
                style.setProperty("top", (rect.top + rect.height / 2) + "px");
                style.setProperty("transform", "translate(8px, -50%)");
                break;
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}
