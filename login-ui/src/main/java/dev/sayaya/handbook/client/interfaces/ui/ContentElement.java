package dev.sayaya.handbook.client.interfaces.ui;

import com.google.gwt.core.client.Scheduler;
import dev.sayaya.handbook.client.domain.Log;
import elemental2.dom.DomGlobal;
import jsinterop.base.JsPropertyMap;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 로그인 화면 메인 컨텐츠.
 *
 * <p><b>책임:</b> 터미널 콘솔에 웰컴 메시지를 출력하고, OAuth 인증 버튼을 표시한다.
 * 내부 이벤트(OAuth 실패, 타이머, 클릭)를 {@code handbook-login-command} CustomEvent 로
 * 발행하여 핸들러가 처리하도록 위임한다.</p>
 */
@Singleton
public class ContentElement {
    private static final String WELCOME_MESSAGE =
            "  ═══════════════════════════════════════════════════════\n" +
            "   _   _                 _ _                 _    \n" +
            "  | | | | __ _ _ __   __| | |__   ___   ___ | | __\n" +
            "  | |_| |/ _` | '_ \\ / _` | '_ \\ / _ \\ / _ \\| |/ /\n" +
            "  |  _  | (_| | | | | (_| | |_) | (_) | (_) |   < \n" +
            "  |_| |_|\\__,_|_| |_|\\__,_|_.__/ \\___/ \\___/|_|\\_\\\n" +
            "\n" +
            "  :: Handbook Project ::                  (v1.0.0)\n" +
            "  ═══════════════════════════════════════════════════════\n" +
            " ";
    private static final int ATTENTION_DELAY_MS = 5000;

    private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> element;
    private final ConsoleElement console;
    private final Log log;
    private final AuthenticationProviderButtonFactory providerFactory;
    private AuthenticationProviderButton[] buttons;
    private double attentionTimer = -1;

    @Inject ContentElement(ConsoleElement console, Log log, AuthenticationProviderButtonFactory providerFactory) {
        this.console = console;
        this.log = log;
        this.providerFactory = providerFactory;
        element = div().css("login-content");
        initialize();
    }

    private void initialize() {
        console.alignCenter(true);
        console.element().style.setProperty("width", "35rem");
        console.element().style.setProperty("height", "0");
        console.element().style.setProperty("margin", "auto");
        element.add(console.element());
        checkOAuthError();
        log.next(WELCOME_MESSAGE);
        Scheduler.get().scheduleFixedDelay(() -> {
            console.element().style.setProperty("height", "20rem");
            console.alignCenter(false);
            Scheduler.get().scheduleFixedDelay(() -> {
                log.next("> SELECT YOUR AUTHENTICATION PROVIDER:");
                log.next("");
                console.close();
                console.alignCenter(true);
                var btnGoogle = providerFactory.button("google");
                buttons = new AuthenticationProviderButton[]{ btnGoogle };
                console.add(btnGoogle);
                setupKeyboardNavigation();
                Scheduler.get().scheduleDeferred(() -> buttons[0].select());
                startAttentionTimer();
                return false;
            }, 100);
            return false;
        }, 100);
    }

    private void setupKeyboardNavigation() {
        for (int i = 0; i < buttons.length; i++) {
            var idx = i;
            buttons[i].on(EventType.keydown, evt -> {
                var key = evt.key;
                if ("ArrowUp".equals(key) || "ArrowDown".equals(key)) {
                    var next = idx + ("ArrowDown".equals(key) ? 1 : -1);
                    if (next >= 0 && next < buttons.length) buttons[next].select();
                }
            });
            buttons[i].onClick(evt -> {
                cancelAttentionTimer();
                LoginCommandRouter.dispatch(JsPropertyMap.of("type", "progress"));
            });
        }
    }

    private void checkOAuthError() {
        var error = getUrlParam("error");
        if (error != null) {
            LoginCommandRouter.dispatch(JsPropertyMap.of("type", "notify", "level", "error", "message", "Authentication failed: " + error));
        }
    }

    private void startAttentionTimer() {
        attentionTimer = DomGlobal.setTimeout(e ->
            LoginCommandRouter.dispatch(JsPropertyMap.of("type", "attention")), ATTENTION_DELAY_MS);
    }

    private void cancelAttentionTimer() {
        if (attentionTimer >= 0) {
            DomGlobal.clearTimeout(attentionTimer);
            attentionTimer = -1;
        }
    }

    private static String getUrlParam(String name) {
        var search = DomGlobal.window.location.search;
        if (search == null || search.isEmpty()) return null;
        for (var param : search.substring(1).split("&")) {
            var parts = param.split("=", 2);
            if (parts.length == 2 && parts[0].equals(name)) return elemental2.core.Global.decodeURIComponent(parts[1]);
        }
        return null;
    }

    public elemental2.dom.HTMLElement element() {
        return element.element();
    }
}
