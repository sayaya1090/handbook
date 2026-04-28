package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.domain.*;

import dev.sayaya.handbook.domain.Log;
import dev.sayaya.handbook.client.usecase.LoginCommandDispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * notify 커맨드 핸들러 — 콘솔에 메시지를 출력한다.
 */
@Singleton
public class LoginNotifyHandler {
    @Inject LoginNotifyHandler(LoginCommandDispatcher dispatcher, Log log, ConsoleElement console) {
        dispatcher.notifications().subscribe(cmd -> {
            if (cmd == null) return;
            var level = cmd.level();
            var msg = cmd.message();
            if ("error".equals(level)) log.next("[ERROR] " + msg);
            else if ("warning".equals(level)) log.next("[WARN] " + msg);
            else log.next(msg);
            console.close();
        });
    }
}
