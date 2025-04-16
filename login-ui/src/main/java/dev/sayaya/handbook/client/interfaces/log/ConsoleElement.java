package dev.sayaya.handbook.client.interfaces.log;

import dev.sayaya.handbook.client.usecase.Log;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static org.jboss.elemento.Elements.div;

@Singleton
public class ConsoleElement implements IsElement<HTMLDivElement> {
    private static final int MAX_LINES = 1000;
    private boolean alignCenter = false;
    private LineElement last = null;
    private final HTMLContainerBuilder<HTMLDivElement> lines = div().style("margin: auto;");
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> div = div().css("console").add(lines);
    @Inject ConsoleElement(Log log) {
        log.subscribe(this::println);
    }
    private void println(String text) {
        Arrays.stream(text.split("\n")).map(LineElement::print).forEach(line->{
            close();
            if(alignCenter) line.css("line-center");
            last = line;
            lines.add(last);
        });
        removeExcessLinesIfNeeded();
        element().scrollTop = element().scrollHeight;
    }
    public ConsoleElement alignCenter(boolean alignCenter) {
        this.alignCenter = alignCenter;
        return this;
    }
    public void close() {
        if(last!=null) last.close();
    }
    private void removeExcessLinesIfNeeded() {
        var currentLineCount = lines.element().childElementCount;
        while (currentLineCount > MAX_LINES) {
            HTMLDivElement container = lines.element();
            if (container.firstChild != null) {
                container.removeChild(container.firstChild);
                currentLineCount--;
            } else break;
        }
    }
}
