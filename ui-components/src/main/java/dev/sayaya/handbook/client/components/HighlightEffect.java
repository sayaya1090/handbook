package dev.sayaya.handbook.client.components;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * 범용 DOM 요소 강조 효과.
 * CSS 선택자로 요소를 찾아 pulse 애니메이션 클래스를 토글한다.
 */
public class HighlightEffect {
    private static final String HIGHLIGHT_CLASS = "ui-highlight";
    private Element currentTarget;

    /** 대상 요소에 강조 효과를 적용한다. 이전 강조는 자동 해제된다. */
    public void highlight(String selector) {
        clear();
        if (selector == null) return;
        Element el = DomGlobal.document.querySelector(selector);
        if (el != null) {
            el.classList.add(HIGHLIGHT_CLASS);
            currentTarget = el;
        }
    }

    /** 현재 강조를 해제한다. */
    public void clear() {
        if (currentTarget != null) {
            currentTarget.classList.remove(HIGHLIGHT_CLASS);
            currentTarget = null;
        }
    }
}
