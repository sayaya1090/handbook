package dev.sayaya.handbook.client.interfaces.drawer;

import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

/**
 * 네비게이션 레일 요소의 공통 상태 전환 인터페이스.
 *
 * <p><b>책임:</b> expand/collapse/hide 상태 전환 시 HTML 속성을 토글한다.
 * 모바일 여부는 별도의 {@code [mobile]} 속성으로 관리되며 이 인터페이스가 건드리지 않는다.</p>
 */
public interface NavigationRailElement<E extends NavigationRailElement<E>> extends IsElement<HTMLElement> {
    default void expand() {
        element().setAttribute("expand", true);
        element().removeAttribute("collapse");
        element().removeAttribute("hide");
    }
    default void collapse() {
        element().removeAttribute("expand");
        element().setAttribute("collapse", true);
        element().removeAttribute("hide");
    }
    default void hide() {
        element().removeAttribute("expand");
        element().removeAttribute("collapse");
        element().setAttribute("hide", true);
    }
}
