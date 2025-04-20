package dev.sayaya.handbook.client.interfaces.create;

import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.elemento.Elements.div;

@Singleton
public class ContentElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> div = div().style("display: flex;height: -webkit-fill-available;");
    private final DialogElement dialog;
    @Inject ContentElement(DialogElement dialog) {
        this.dialog = dialog;
        add(dialog.style("height: 0rem;"));
        setTimeout(e-> initialize(), 100);
    }
    private void initialize() {
        dialog.element().style.height = CSSProperties.HeightUnionType.of("25rem");
    }
}
