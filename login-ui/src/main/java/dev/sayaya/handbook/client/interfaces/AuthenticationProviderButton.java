package dev.sayaya.handbook.client.interfaces;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.interfaces.api.OAuthApi;
import dev.sayaya.ui.dom.MdButtonElement;
import dev.sayaya.ui.elements.ButtonElementBuilder.TextButtonElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static elemental2.dom.DomGlobal.window;

public class AuthenticationProviderButton implements IsElement<MdButtonElement> {
    @Delegate private final TextButtonElementBuilder btnLogin;
    private final OAuthApi api;
    @AssistedInject AuthenticationProviderButton(@Assisted String provider, OAuthApi api) {
        btnLogin = button().text().id(provider).css("button")
                .add(provider.toUpperCase())
                .icon(dev.sayaya.ui.elements.IconElementBuilder.icon()
                        .css("fa-brands", "fa-"+provider))
                .onClick(evt -> login(provider)).style("margin-bottom: 1rem;");
        this.api = api;
    }
    private void login(String provider) {
        window.location.href = "oauth2/authorization/" + provider;
    }
}
