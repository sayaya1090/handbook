package dev.sayaya.handbook.client.components;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.ScrollIntoViewOptions;

/**
 * 범용 스크롤 + 도착 강조 효과 컴포넌트.
 *
 * <p><b>책임:</b> CSS 선택자로 DOM 요소를 찾아 scrollIntoView({ behavior: 'smooth' })로 스크롤하고, 2초간 도착 강조 클래스를 부여한다.</p>
 * <p><b>의존관계:</b> <ul><li>{@link elemental2.dom.DomGlobal#document} — DOM 요소 쿼리</li></ul></p>
 * <p><b>주의:</b> scrollSmooth()는 Elemental2 ScrollIntoViewOptions을 사용하여 부드러운 스크롤을 수행한다.</p>
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

    /** 지정된 요소로 부드럽게 스크롤하고 포커스를 이동한다. */
    private void scrollSmooth(Element el) {
        ScrollIntoViewOptions opts = ScrollIntoViewOptions.create();
        opts.setBehavior("smooth");
        opts.setBlock("center");
        el.scrollIntoView(opts);
        if (el instanceof elemental2.dom.HTMLElement) {
            ((elemental2.dom.HTMLElement) el).focus();
        }
    }
}
