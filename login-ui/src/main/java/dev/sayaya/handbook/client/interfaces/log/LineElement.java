package dev.sayaya.handbook.client.interfaces.log;

import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

public class LineElement implements IsElement<HTMLDivElement> {
    static LineElement print(String text) {
        var line = new LineElement();
        line.element().innerHTML = text;
        return line;
    }
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("line");
    private LineElement() {
        this.element().innerHTML = "";
    }
    void close() {
        element().style.borderRight = "0px hidden";
    }
}
