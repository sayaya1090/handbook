package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.AgentSessionState;
import dev.sayaya.handbook.client.usecase.AgentApiPort;
import dev.sayaya.handbook.client.usecase.AgentSession;
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
        switch (state) {
            case IDLE:
            case COMPLETED:
            case ABORTED:
                textField.disabled(false);
                textField.label(labels.getOrDefault("agent.label.idle", "How can I help you?"));
                textField.placeholder(labels.getOrDefault("agent.placeholder", "Ask in natural language"));
                sendBtn.style.set("display", "inline-flex");
                abortBtn.style.set("display", "none");
                break;
            case PLANNING:
                textField.disabled(true);
                textField.label(labels.getOrDefault("agent.label.planning", "Analyzing your request..."));
                sendBtn.style.set("display", "none");
                abortBtn.style.set("display", "inline-flex");
                break;
            case EXECUTING:
                textField.disabled(true);
                textField.label(labels.getOrDefault("agent.label.executing", "Executing..."));
                sendBtn.style.set("display", "none");
                abortBtn.style.set("display", "inline-flex");
                break;
            case AWAITING_CONFIRM:
                textField.disabled(true);
                textField.label(labels.getOrDefault("agent.label.confirming", "Waiting for confirmation..."));
                sendBtn.style.set("display", "none");
                abortBtn.style.set("display", "inline-flex");
                break;
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}
