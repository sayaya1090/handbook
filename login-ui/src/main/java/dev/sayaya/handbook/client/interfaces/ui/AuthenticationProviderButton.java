package dev.sayaya.handbook.client.interfaces.ui;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import elemental2.dom.DomGlobal;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.i;

public class AuthenticationProviderButton {
    private final HTMLContainerBuilder<elemental2.dom.HTMLButtonElement> btnLogin;

    @AssistedInject
    AuthenticationProviderButton(@Assisted String provider) {
        btnLogin = button().css("btn-oauth", "btn-" + provider)
                .add(i().css("fa-brands", "fa-" + provider))
                .add(" Sign in with " + capitalize(provider))
                .on(EventType.click, e -> login(provider));
    }

    private void login(String provider) {
        DomGlobal.window.location.assign("oauth2/authorization/" + provider);
    }

    public elemental2.dom.HTMLElement element() {
        return btnLogin.element();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
