package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.domain.*;

import dev.sayaya.handbook.client.domain.Log;
import dev.sayaya.handbook.client.usecase.LoginCommandDispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * attention 커맨드 핸들러 — 콘솔에 안내 메시지를 출력한다.
 */
@Singleton
public class LoginAttentionHandler {
    private static final String DEFAULT_MESSAGE = "↑ Click the button above to sign in";

    @Inject LoginAttentionHandler(LoginCommandDispatcher dispatcher, Log log, ConsoleElement console) {
        dispatcher.attentions().subscribe(cmd -> {
            if (cmd == null) return;
            var msg = cmd.message();
            log.next("");
            log.next("  " + (msg != null ? msg : DEFAULT_MESSAGE));
            console.close();
        });
    }
}
