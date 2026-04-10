package dev.sayaya.handbook.client.interfaces.ui;

import com.google.gwt.core.client.Scheduler;
import dev.sayaya.handbook.client.domain.Log;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class ContentElement {
    private static final String WELCOME_MESSAGE =
            " _   _                 _ _                 _    \n" +
            "| | | | __ _ _ __   __| | |__   ___   ___ | | __\n" +
            "| |_| |/ _` | '_ \\ / _` | '_ \\ / _ \\ / _ \\| |/ /\n" +
            "|  _  | (_| | | | | (_| | |_) | (_) | (_) |   < \n" +
            "|_| |_|\\__,_|_| |_|\\__,_|_.__/ \\___/ \\___/|_|\\_\\\n" +
            "                                   v1.0.0";

    private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> element;
    private final ConsoleElement console;
    private final Log log;
    private final AuthenticationProviderButtonFactory providerFactory;

    @Inject ContentElement(ConsoleElement console, Log log, AuthenticationProviderButtonFactory providerFactory) {
        this.console = console;
        this.log = log;
        this.providerFactory = providerFactory;
        element = div().css("login-content");
        initialize();
    }

    private void initialize() {
        element.add(console.element());
        log.next(WELCOME_MESSAGE);
        Scheduler.get().scheduleFixedDelay(() -> {
            console.element().style.setProperty("height", "20rem");
            console.alignCenter(false);
            Scheduler.get().scheduleFixedDelay(() -> {
                log.next("> SELECT YOUR AUTHENTICATION PROVIDER:");
                console.close();
                console.alignCenter(true);
                var btnDiv = div().css("oauth-buttons");
                btnDiv.add(providerFactory.button("google").element());
                element.add(btnDiv.element());
                return false;
            }, 100);
            return false;
        }, 100);
    }

    public elemental2.dom.HTMLElement element() {
        return element.element();
    }
}
