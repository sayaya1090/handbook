package dev.sayaya.handbook.client.interfaces.ui;

import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

public class LineElement {
    private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> element;
    private LineElement(String text) {
        element = div().css("line");
        element.element().textContent = text;
    }
    public static LineElement print(String text) {
        return new LineElement(text);
    }
    public elemental2.dom.HTMLElement element() {
        return element.element();
    }
    public void close() {
        element.css("closed");
    }
}
