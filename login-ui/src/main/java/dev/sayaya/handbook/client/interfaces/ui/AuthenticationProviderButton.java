package dev.sayaya.handbook.client.interfaces.ui;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.ui.elements.ButtonElementBuilder.TextButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLAudioElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static elemental2.dom.DomGlobal.*;

/**
 * OAuth2 인증 제공자 로그인 버튼.
 *
 * <p><b>책임:</b> 제공자 이름(google 등)에 따라 MD3 TextButton 을 생성하고,
 * 클릭 시 {@code /oauth2/authorization/{provider}} 로 이동한다.
 * 포커스 시 focus-ring 을 강제 표시하고, 사운드를 재생한다.</p>
 */
public class AuthenticationProviderButton implements IsElement<HTMLElement> {
    @Delegate private final TextButtonElementBuilder _this;
    private static final HTMLAudioElement beep = createAudio("wav/beep.mp3");
    private static final HTMLAudioElement start = createAudio("wav/start.mp3");

    @AssistedInject
    AuthenticationProviderButton(@Assisted String provider) {
        _this = button().text()
                .css("btn-oauth", "btn-" + provider)
                .add(provider.toUpperCase())
                .icon(IconElementBuilder.icon().css("fa-brands", "fa-" + provider))
                .onClick(evt -> {
                    replay(start);
                    setTimeout(e -> window.location.href = "oauth2/authorization/" + provider, 300);
                });
        element().addEventListener("focus", evt -> select());
    }

    /** 이 버튼에 포커스를 주고 MD3 focus-ring 을 강제 표시하며 beep 사운드를 재생한다. */
    public void select() {
        element().focus();
        replay(beep);
        try {
            element().shadowRoot.querySelector("md-focus-ring")
                    .setAttribute("visible", "true");
        } catch (Exception ignore) {}
    }

    private static void replay(HTMLAudioElement audio) {
        audio.pause();
        audio.currentTime = 0;
        audio.play();
    }

    private static HTMLAudioElement createAudio(String src) {
        var audio = (HTMLAudioElement) document.createElement("audio");
        audio.src = src;
        document.body.appendChild(audio);
        return audio;
    }
}
