package dev.sayaya.handbook.client.interfaces.drawer;

import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

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
