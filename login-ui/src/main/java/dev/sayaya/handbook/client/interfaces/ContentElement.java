package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.interfaces.log.ConsoleElement;
import dev.sayaya.handbook.client.usecase.Log;
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
    private final static String WELCOME_MESSAGE = """
            ═══════════════════════════════════════════════════════
             _   _                 _ _                 _   \s
            | | | | __ _ _ __   __| | |__   ___   ___ | | __
            | |_| |/ _` | '_ \\ / _` | '_ \\ / _ \\ / _ \\| |/ /
            |  _  | (_| | | | | (_| | |_) | (_) | (_) |   <\s
            |_| |_|\\__,_|_| |_|\\__,_|_.__/ \\___/ \\___/|_|\\_\\
            
            :: Handbook Project ::                  (v1.0.0)
            ═══════════════════════════════════════════════════════
            
            \s
            """;
    private final ConsoleElement console;
    private final Log log;
    private final AuthenticationProviderButtonFactory providerFactory;
    @Inject ContentElement(ConsoleElement console, Log log, AuthenticationProviderButtonFactory providerFactory) {
        this.console = console;
        this.log = log;
        this.providerFactory = providerFactory;
        add(console.alignCenter(true).style("width: 35rem; height: 0rem; margin: auto;"));
        log.next(WELCOME_MESSAGE);
        setTimeout(e-> initialize(), 100);
    }
    private void initialize() {
        console.element().style.height = CSSProperties.HeightUnionType.of("20rem");
        console.alignCenter(false);
        setTimeout(e-> {
            log.next("> SELECT YOUR AUTHENTICATION PROVIDER:");
            console.close();
            console.alignCenter(true);
            console.add(providerFactory.button("google"));
        }, 100);
    }
}
