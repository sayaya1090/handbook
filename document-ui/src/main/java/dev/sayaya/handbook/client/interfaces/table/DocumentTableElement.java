package dev.sayaya.handbook.client.interfaces.table;

import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class DocumentTableElement implements IsElement<HTMLDivElement> {
    private final HandsontableConfiguration config = new HandsontableConfiguration();
    private final HandsontableElement table;
    @Inject DocumentTableElement() {
        config.data = new String[][] {
                {"1", "2", "3"},
                {"4", "5", "6"},
                {"7", "8", "9"},
        };
        config.stretchH = "all";
        config.colHeaders= new String[] {"A", "B", "C"};
        config.rowHeaders = true;
        config.width = "100%";
        table = new HandsontableElement(config);
        // config.setHeight("100%");
    }

    @Override
    public HTMLDivElement element() {
        return table.element();
    }
}
