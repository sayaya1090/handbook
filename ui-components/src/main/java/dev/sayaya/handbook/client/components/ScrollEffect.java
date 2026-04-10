package dev.sayaya.handbook.client.components;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * 범용 스크롤 + 도착 강조 효과.
 * CSS 선택자로 요소를 찾아 부드럽게 스크롤하고, 도착 후 잠깐 강조한다.
 */
public class ScrollEffect {
    private static final String ARRIVED_CLASS = "ui-scroll-arrived";

    /** 대상 요소로 부드럽게 스크롤하고 도착 강조를 표시한다. */
    public void scrollTo(String selector) {
        if (selector == null) return;
        Element el = DomGlobal.document.querySelector(selector);
        if (el != null) {
            scrollSmooth(el);
            el.classList.add(ARRIVED_CLASS);
            DomGlobal.setTimeout(e -> el.classList.remove(ARRIVED_CLASS), 2000);
        }
    }

    private native void scrollSmooth(Element el) /*-{
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        if (el.focus) el.focus();
    }-*/;
}
