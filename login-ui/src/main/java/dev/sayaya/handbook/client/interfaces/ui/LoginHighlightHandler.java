package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.domain.*;

import dev.sayaya.handbook.client.usecase.LoginCommandDispatcher;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * highlight 커맨드 핸들러 — 대상 요소에 강조 클래스를 토글한다.
 */
@Singleton
public class LoginHighlightHandler {
    @Inject LoginHighlightHandler(LoginCommandDispatcher dispatcher) {
        dispatcher.highlights().subscribe(cmd -> {
            if (cmd == null) return;
            var target = cmd.target();
            if (target == null) return;
            var el = DomGlobal.document.querySelector(target);
            if (el != null) el.classList.toggle("login-highlight");
        });
    }
}
