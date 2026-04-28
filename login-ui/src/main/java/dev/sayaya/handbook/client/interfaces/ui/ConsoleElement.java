package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.domain.Log;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class ConsoleElement {
    private static final int MAX_LINES = 50;
    @Delegate
    private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> element;
    private boolean alignCenter = false;
    private LineElement last;

    @Inject ConsoleElement(Log log) {
        element = div().css("console");
        log.subscribe(this::println);
    }

    private void println(String text) {
        if (last != null) last.close();
        last = LineElement.print(text);
        if (alignCenter) last.element().classList.add("center");
        element.add(last.element());
        removeExcessLinesIfNeeded();
    }

    public ConsoleElement alignCenter(boolean alignCenter) {
        this.alignCenter = alignCenter;
        return this;
    }

    public void close() {
        if (last != null) last.close();
    }

    private void removeExcessLinesIfNeeded() {
        var el = element.element();
        while (el.childElementCount > MAX_LINES) {
            el.removeChild(el.firstElementChild);
        }
    }
}
