package dev.sayaya.handbook.client.interfaces.create;

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
    @Inject ContentElement(DialogElement dialog) {
        add(dialog.style("width: 35rem; height: 0rem; margin: auto;"));
        //log.next(WELCOME_MESSAGE);
        setTimeout(e-> initialize(), 100);
    }
    private void initialize() {
        /*console.element().style.height = CSSProperties.HeightUnionType.of("20rem");
        console.alignCenter(false);
        setTimeout(e-> {
            log.next("> SELECT YOUR AUTHENTICATION PROVIDER:");
            console.close();
            console.alignCenter(true);
            console.add(providerFactory.button("google"));
        }, 100);*/
    }
}
