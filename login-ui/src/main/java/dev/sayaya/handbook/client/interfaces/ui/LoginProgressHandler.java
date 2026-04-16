package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.domain.*;

import dev.sayaya.handbook.client.domain.Log;
import dev.sayaya.handbook.client.usecase.LoginCommandDispatcher;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * progress 커맨드 핸들러 — 버튼 비활성화 + 진행 메시지 출력.
 */
@Singleton
public class LoginProgressHandler {
    private static final String DEFAULT_MESSAGE = "Connecting to authentication provider...";

    @Inject LoginProgressHandler(LoginCommandDispatcher dispatcher, Log log, ConsoleElement console) {
        dispatcher.progressUpdates().subscribe(cmd -> {
            if (cmd == null) return;
            var btns = DomGlobal.document.querySelectorAll(".btn-oauth");
            for (int i = 0; i < btns.length; i++) {
                ((elemental2.dom.HTMLElement) btns.item(i)).setAttribute("disabled", "true");
            }
            var msg = cmd.description();
            log.next("");
            log.next("> " + (msg != null ? msg : DEFAULT_MESSAGE));
            console.close();
        });
    }
}
