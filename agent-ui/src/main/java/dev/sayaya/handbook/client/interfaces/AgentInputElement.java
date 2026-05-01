package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentApiPort;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.handbook.domain.AgentSessionState;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EnumMap;

import static org.jboss.elemento.Elements.div;

/**
 * 에이전트에게 자연어 요청을 보내는 메인 입력 UI.
 *
 * <p><b>책임:</b> 하단 중앙에 고정된 입력 필드를 제공하고, 세션 상태에 따라 입력/비활성/중단 버튼을 전환한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentApiPort} — 세션 시작, 응답 전송, 중단 요청</li>
 *   <li>{@link AgentSession} — 세션 상태 구독</li>
 *   <li>{@link ConfirmDialogElement} — 사용자 확인 응답 콜백 연결</li>
 *   <li>{@link LabelProvider} — 입력 필드 라벨/플레이스홀더 다국어 처리</li>
 *   <li>{@link ViewportObserver} — 모바일 뷰포트 감지, 가상 키보드 높이 조정</li>
 * </ul></p>
 */
@Singleton
public class AgentInputElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder textField;
    private final HTMLElement sendBtn;
    private final HTMLElement abortBtn;
    private final AgentApiPort api;
    private Labels labels = Labels.empty();
    private AgentSessionState currentState = AgentSessionState.IDLE;
    private String workspace;

    @Inject
    AgentInputElement(AgentApiPort api, AgentSession session, ConfirmDialogElement confirmDialog, LabelProvider labelProvider, ViewportObserver viewport) {
        this.api = api;

        textField = TextFieldElementBuilder.textField().outlined()
                .label("")
                .placeholder("")
                .css("agent-input-field");

        sendBtn = ButtonElementBuilder.button().filled()
                .text("")
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-paper-plane-top"))
                .css("agent-input-send")
                .on(EventType.click, e -> send())
                .element();

        abortBtn = ButtonElementBuilder.button().outlined()
                .text("")
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-stop"))
                .css("agent-input-abort")
                .on(EventType.click, e -> abort())
                .element();
        abortBtn.style.set("display", "none");

        HTMLDivElement inputWrapper = div().css("agent-input-wrapper")
                .add(textField)
                .add(sendBtn)
                .add(abortBtn)
                .element();

        root = div().css("agent-input-container")
                .add(inputWrapper)
                .element();

        textField.on(EventType.keydown, e -> {
            if ("Enter".equals(((elemental2.dom.KeyboardEvent) e).key)) send();
        });

        session.state().subscribe(this::onStateChange);
        labelProvider.subscribe(l -> {
            this.labels = l;
            applyLabels();
        });
        confirmDialog.onResponse(response -> {
            if (workspace != null) api.respond(workspace, response);
        });
        viewport.isMobile().subscribe(this::onMobileChange);
    }

    private void onMobileChange(boolean mobile) {
        if (mobile) initVirtualKeyboardListener();
    }

    /**
     * 모바일 가상 키보드 높이를 CSS 변수로 반영하는 리스너를 등록한다.
     * visualViewport API가 지원되는 브라우저에서만 동작한다.
     */
    private static void initVirtualKeyboardListener() {
        Object vv = jsinterop.base.Js.asPropertyMap(elemental2.dom.DomGlobal.window).get("visualViewport");
        if (vv == null) return;
        elemental2.dom.EventTarget viewport = jsinterop.base.Js.cast(vv);
        viewport.addEventListener("resize", e -> {
            jsinterop.base.JsPropertyMap<?> vvMap = jsinterop.base.Js.cast(vv);
            double vvHeight = ((jsinterop.base.Any) vvMap.get("height")).asDouble();
            double innerHeight = elemental2.dom.DomGlobal.window.innerHeight;
            double keyboardHeight = Math.max(0, innerHeight - vvHeight);
            elemental2.dom.DomGlobal.document.documentElement.style.setProperty("--keyboard-height", keyboardHeight + "px");
        });
    }

    private void applyLabels() {
        sendBtn.textContent = labels.getOrDefault("agent.send", "Send");
        abortBtn.textContent = labels.getOrDefault("agent.abort", "Abort");
        onStateChange(currentState);
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    private void send() {
        String text = textField.value();
        if (text == null || text.trim().isEmpty() || workspace == null) return;
        textField.value("");
        api.startSession(workspace, text.trim());
    }

    private void abort() {
        if (workspace != null) api.abort(workspace);
    }

    private void onStateChange(AgentSessionState state) {
        this.currentState = state;
        var p = PRESENTATIONS.get(state);
        if (p == null) return;
        textField.disabled(p.disabled);
        textField.label(labels.getOrDefault(p.labelKey, ""));
        if (p.placeholderKey != null) {
            textField.placeholder(labels.getOrDefault(p.placeholderKey, ""));
        }
        sendBtn.style.set("display", p.showSend ? "inline-flex" : "none");
        abortBtn.style.set("display", p.showSend ? "none" : "inline-flex");
    }

    /** state 별 UI 표현 맵. 새 state 추가 시 switch 수정 대신 map 엔트리만 추가하면 된다. */
    private static final EnumMap<AgentSessionState, Presentation> PRESENTATIONS = buildPresentations();

    private static EnumMap<AgentSessionState, Presentation> buildPresentations() {
        var map = new EnumMap<AgentSessionState, Presentation>(AgentSessionState.class);
        var idle = new Presentation(false, "agent.label.idle", "agent.placeholder", true);
        map.put(AgentSessionState.IDLE, idle);
        map.put(AgentSessionState.COMPLETED, idle);
        map.put(AgentSessionState.ABORTED, idle);
        map.put(AgentSessionState.PLANNING, new Presentation(true, "agent.label.planning", null, false));
        map.put(AgentSessionState.EXECUTING, new Presentation(true, "agent.label.executing", null, false));
        map.put(AgentSessionState.AWAITING_CONFIRM, new Presentation(true, "agent.label.confirming", null, false));
        return map;
    }

    private static final class Presentation {
        final boolean disabled;
        final String labelKey;
        final String placeholderKey;
        final boolean showSend;
        Presentation(boolean disabled, String labelKey, String placeholderKey, boolean showSend) {
            this.disabled = disabled;
            this.labelKey = labelKey;
            this.placeholderKey = placeholderKey;
            this.showSend = showSend;
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}
