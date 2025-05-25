package dev.sayaya.handbook.client.interfaces.table;

import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

public class HandsontableElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> div = div();
    @Delegate private final Handsontable table;
    public HandsontableElement(HandsontableConfiguration config) {
        this.table = new Handsontable(div.element(), config);
    }
}
