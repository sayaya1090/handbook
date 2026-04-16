package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.domain.*;
import dev.sayaya.handbook.client.usecase.LoginCommandDispatcher;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * {@code handbook-login-command} CustomEvent 를 수신하여 타입별 Subject 로 라우팅.
 *
 * <p><b>책임:</b> window CustomEvent → type 분기 → 도메인 VO cast → Subject 발행.</p>
 */
@Singleton
public class LoginCommandRouter implements LoginCommandDispatcher {
    public static final String EVENT_NAME = "handbook-login-command";
    private final BehaviorSubject<NotifyCommand> notifySubject = behavior(null);
    private final BehaviorSubject<AttentionCommand> attentionSubject = behavior(null);
    private final BehaviorSubject<HighlightCommand> highlightSubject = behavior(null);
    private final BehaviorSubject<ProgressCommand> progressSubject = behavior(null);

    @Inject LoginCommandRouter() {
        DomGlobal.window.addEventListener(EVENT_NAME, evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            if (ce.detail == null) return;
            JsPropertyMap<?> cmd = Js.cast(ce.detail);
            route(cmd);
        });
    }

    private void route(JsPropertyMap<?> cmd) {
        var type = (String) cmd.get("type");
        if (type == null) return;
        switch (type) {
            case "notify" -> notifySubject.next(Js.cast(cmd));
            case "attention" -> attentionSubject.next(Js.cast(cmd));
            case "highlight" -> highlightSubject.next(Js.cast(cmd));
            case "progress" -> progressSubject.next(Js.cast(cmd));
        }
    }

    @Override public Observable<NotifyCommand> notifications() { return notifySubject.asObservable(); }
    @Override public Observable<AttentionCommand> attentions() { return attentionSubject.asObservable(); }
    @Override public Observable<HighlightCommand> highlights() { return highlightSubject.asObservable(); }
    @Override public Observable<ProgressCommand> progressUpdates() { return progressSubject.asObservable(); }

    @SuppressWarnings("unchecked")
    public static void dispatch(JsPropertyMap<Object> detail) {
        var init = (CustomEventInit<Object>) Js.cast(CustomEventInit.create());
        init.setDetail(detail);
        init.setBubbles(false);
        DomGlobal.window.dispatchEvent(new CustomEvent<>(EVENT_NAME, init));
    }
}
