package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.usecase.ProgressSharing;
import elemental2.dom.DomGlobal;

/**
 * agent-ui 독립 GWT 모듈의 진입점.
 *
 * <p>shell-ui 가 window 브릿지를 게시한 뒤 {@code handbook-shell-ready} CustomEvent 를
 * 발행하면 그때 Dagger 컴포넌트를 생성하고 초기화한다. shell 이 이미 준비된 상태라면
 * 즉시 부팅한다.</p>
 */
public class AgentApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        DomGlobal.window.addEventListener("handbook-shell-ready", e -> boot());
        boot();
    }

    private void boot() {
        DaggerAgentComponent.create().initializer().initialize();
    }
}
