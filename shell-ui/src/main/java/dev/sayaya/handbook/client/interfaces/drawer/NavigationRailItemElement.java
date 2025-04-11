package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.*;
import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.FocusRingElementBuilder.focusRing;
import static dev.sayaya.ui.elements.RippleElementBuilder.ripple;
import static org.jboss.elemento.Elements.*;

public abstract class NavigationRailItemElement implements IsElement<HTMLElement> {
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder collapse = button().icon().css("collapse").toggle(true);
    private final HTMLContainerBuilder<HTMLLIElement> expand = li().css("expand").attr("tabindex", "0");
    private final HTMLContainerBuilder<HTMLElement> item = mdItem();
    private final HTMLContainerBuilder<HTMLElement> _this = span().css("item").add(collapse).add(expand.add(ripple()).add(focusRing()).add(item));
    private static HTMLContainerBuilder<HTMLElement> mdItem() {
        var style = htmlElement("style", HTMLStyleElement.class);
        style.element().innerHTML = ICON_CSS;
        return  htmlContainer("md-item", HTMLElement.class).attr("multiline", true).add(style);
    }
    public void select(boolean value) {
        collapse.element().selected = value;
        if(value) element().setAttribute("selected", true);
        else element().removeAttribute("selected");
    }
    public NavigationRailItemElement icon(Element icon) {
        collapse.add(icon);
        return this;
    }
    public NavigationRailItemElement icon(IconElementBuilder icon) {
        return this.icon(icon.element());
    }
    public NavigationRailItemElement start(IconElementBuilder icon) {
        return start(icon.element());
    }
    public NavigationRailItemElement start(Element icon) {
        icon.setAttribute("slot", "start");
        icon.classList.add("icon");
        item.add(icon);
        return this;
    }
    public NavigationRailItemElement headline(String headline) {
        return headline(div().add(headline));
    }
    private NavigationRailItemElement headline(IsElement<? extends HTMLElement> element) {
        return headline(element.element());
    }
    public NavigationRailItemElement headline(HTMLElement element) {
        element.setAttribute("slot", "headline");
        item.add(element);
        return this;
    }
    public NavigationRailItemElement supportingText(String supportingText) {
        return supportingText(div().add(supportingText));
    }
    public NavigationRailItemElement supportingText(IsElement<? extends HTMLElement> element) {
        return supportingText(element.element());
    }
    public NavigationRailItemElement supportingText(HTMLElement element) {
        element.setAttribute("slot", "supporting-text");
        element.style.color = "var(--md-list-item-label-text-color)";
        item.add(element);
        return this;
    }
    public NavigationRailItemElement trailingSupportingText(String supportingText) {
        return trailingSupportingText(div().add(supportingText));
    }
    public NavigationRailItemElement trailingSupportingText(IsElement<? extends HTMLElement> element) {
        return trailingSupportingText(element.element());
    }
    public NavigationRailItemElement trailingSupportingText(HTMLElement element) {
        element.setAttribute("slot", "trailing-supporting-text");
        item.add(element);
        return this;
    }
    @Override
    public HTMLElement element() {
        return _this.element();
    }
    public <V extends Event> void on(EventType<V, ?> type, EventCallbackFn<V> callback) {
        _this.on(type, callback);
    }
    private final static String ICON_CSS = """
        .icon {
            font-size: var(--md-icon-size, 24px);
            width: var(--md-icon-size, 24px);
            height: var(--md-icon-size, 24px);
            color: inherit;
            font-variation-settings: inherit;
            font-weight: 400;
            font-family: var(--md-icon-font, Material Symbols Outlined);
            display: inline-flex;
            font-style: normal;
            place-items: center;
            place-content: center;
            line-height: 1;
            overflow: hidden;
            letter-spacing: normal;
            text-transform: none;
            user-select: none;
            white-space: nowrap;
            word-wrap: normal;
            flex-shrink: 0;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            -moz-osx-font-smoothing: grayscale;
        }""";
}