package dev.sayaya.handbook.client.interfaces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.handbook.domain.AttentionStyle;
import dev.sayaya.rx.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * SSE로 수신된 JSON 커맨드를 파싱하여 타입별 BehaviorSubject에 발행하는 라우터.
 *
 * <p><b>책임:</b> JSNI JSON.parse()로 커맨드 타입을 판별하고, navigate/highlight/attention/scroll/preview/mutate/notify/progress/await_confirm/complete를 각각의 Subject에 발행한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentSession} — await_confirm/complete 시 세션 상태 전이</li>
 *   <li>{@link BehaviorSubject} — 커맨드별 반응형 스트림</li>
 * </ul></p>
 * <p><b>주의:</b> JSON 파싱은 JSNI로 수행되므로 브라우저 환경에서만 동작한다.</p>
 */
@Singleton
public class CommandRouter implements AgentCommandDispatcher {
    private final AgentSession session;

    private final dev.sayaya.rx.subject.BehaviorSubject<OverlayRequest> overlaySubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<ConfirmRequest> confirmSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<ProgressInfo> progressSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String[]> previewSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String> completeSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String> highlightSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String> scrollSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<NavigateInfo> navigateSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String[]> mutateSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<NotifyInfo> notifySubject = behavior(null);

    @Inject
    public CommandRouter(AgentSession session) {
        this.session = session;
    }

    public void route(String json) {
        try {
            routeNative(json);
        } catch (Exception e) {
            GWT.log("Failed to route command: " + e.getMessage());
        }
    }

    private native void routeNative(String json) /*-{
        try {
            var cmd = JSON.parse(json);
            if (!cmd || !cmd.type) return;

            var self = this;
            switch(cmd.type) {
                case "navigate":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onNavigate(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(
                        cmd.menu || null, cmd.tool || null, cmd.url || null);
                    break;
                case "highlight":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onHighlight(Ljava/lang/String;)(cmd.target || null);
                    break;
                case "attention":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onAttention(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)(
                        cmd.target || null, cmd.style || "PULSE", cmd.message || "", cmd.position || "bottom", !!cmd.dismissable);
                    break;
                case "scroll":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onScroll(Ljava/lang/String;)(cmd.target || null);
                    break;
                case "preview":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onPreviewJs(Lcom/google/gwt/core/client/JsArrayString;)(
                        cmd.changes || []);
                    break;
                case "mutate":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onMutateJs(Lcom/google/gwt/core/client/JsArrayString;)(
                        cmd.changes || []);
                    break;
                case "notify":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onNotify(Ljava/lang/String;Ljava/lang/String;)(
                        cmd.level || "info", cmd.message || "");
                    break;
                case "progress":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onProgress(Ljava/lang/String;DD)(
                        cmd.description || "", cmd.value || 0, cmd.max || 0);
                    break;
                case "await_confirm":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onAwaitConfirmJs(Ljava/lang/String;Lcom/google/gwt/core/client/JsArrayString;)(
                        cmd.description || "", cmd.options || []);
                    break;
                case "complete":
                    self.@dev.sayaya.handbook.client.interfaces.CommandRouter::onComplete(Ljava/lang/String;)(cmd.summary || "");
                    break;
            }
        } catch(e) {
            // JSON parse 실패 등 — Java 레벨 catch로 전파
        }
    }-*/;

    private void onNavigate(String menu, String tool, String url) {
        navigateSubject.next(new NavigateInfo(menu, tool, url));
    }
    private void onHighlight(String target) {
        highlightSubject.next(target);
    }
    private void onAttention(String target, String style, String message, String position, boolean dismissable) {
        AttentionStyle attentionStyle;
        try {
            attentionStyle = AttentionStyle.valueOf(style);
        } catch (IllegalArgumentException e) {
            attentionStyle = AttentionStyle.PULSE;
        }
        overlaySubject.next(new OverlayRequest(target, attentionStyle, message, position, dismissable));
    }
    private void onScroll(String target) {
        scrollSubject.next(target);
    }
    private void onPreviewJs(JsArrayString jsArray) {
        previewSubject.next(toStringArray(jsArray));
    }
    private void onMutateJs(JsArrayString jsArray) {
        mutateSubject.next(toStringArray(jsArray));
    }
    private void onNotify(String level, String message) {
        notifySubject.next(new NotifyInfo(level, message));
    }
    private void onProgress(String description, double value, double max) {
        progressSubject.next(new ProgressInfo(description, value, max));
    }
    private void onAwaitConfirmJs(String description, JsArrayString jsArray) {
        session.stateObserver().next(AgentSessionState.AWAITING_CONFIRM);
        confirmSubject.next(new ConfirmRequest(description, toStringArray(jsArray)));
    }
    private void onComplete(String summary) {
        session.stateObserver().next(AgentSessionState.COMPLETED);
        completeSubject.next(summary);
    }

    private static String[] toStringArray(JsArrayString jsArray) {
        if (jsArray == null) return new String[0];
        String[] result = new String[jsArray.length()];
        for (int i = 0; i < jsArray.length(); i++) {
            result[i] = jsArray.get(i);
        }
        return result;
    }

    @Override public Observable<OverlayRequest> overlayRequests() { return overlaySubject; }
    @Override public Observable<ConfirmRequest> confirmRequests() { return confirmSubject; }
    @Override public Observable<ProgressInfo> progressUpdates() { return progressSubject; }
    @Override public Observable<String[]> previewRequests() { return previewSubject; }
    @Override public Observable<String> completions() { return completeSubject; }
    @Override public Observable<String> highlights() { return highlightSubject; }
    @Override public Observable<String> scrollTargets() { return scrollSubject; }
    @Override public Observable<NavigateInfo> navigations() { return navigateSubject; }
    @Override public Observable<String[]> mutations() { return mutateSubject; }
    @Override public Observable<NotifyInfo> notifications() { return notifySubject; }
}
