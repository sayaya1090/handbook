package dev.sayaya.handbook.client.interfaces.ui;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.i;

public class AuthenticationProviderButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @AssistedInject
    AuthenticationProviderButton(@Assisted String provider) {
        root = ButtonElementBuilder.button().outlined()
                .css("btn-oauth", "btn-" + provider)
                .element();
        root.append(i().css("fa-brands", "fa-" + provider).element());
        root.append(DomGlobal.document.createTextNode(" Sign in with " + capitalize(provider)));
        root.addEventListener("click", e -> login(provider));
    }

    private void login(String provider) {
        DomGlobal.window.location.assign("oauth2/authorization/" + provider);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
