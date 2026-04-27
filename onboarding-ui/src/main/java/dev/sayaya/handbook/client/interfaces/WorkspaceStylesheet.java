package dev.sayaya.handbook.client.interfaces;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLLinkElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * workspace-ui 전용 스타일시트({@code css/workspace.css})를 런타임에 document.head 에 주입한다.
 *
 * <p><b>책임:</b> workspace-ui 모듈이 shell 에서 동적으로 로드될 때 자기 CSS 를
 * 스스로 책임지고 붙인다. shell 은 shell.css 만 주입하므로 workspace 카드의
 * {@code .ws-dialog / .ws-section / .ws-section-active / .ws-section-create / .ws-header …}
 * 스타일은 이 컴포넌트가 없으면 영구 미적용 상태가 된다.</p>
 *
 * <p><b>조립 원칙:</b> shell-ui 의 {@code ShellStylesheet} 와 동일한 패턴 — 컴포넌트
 * 단위 DOM 자산 주입. Dagger 가 @Singleton 으로 생성하며
 * {@link dev.sayaya.handbook.client.onboarding.ContentElement} 가 생성자 파라미터로
 * 받아 entry 시점에 주입이 완료된다.</p>
 *
 * <p><b>주의:</b> MARKER_ID 로 중복 주입을 방지한다. GWT devMode 리로드나 같은 모듈
 * 재진입 시 link 태그가 중첩되지 않도록 한다.</p>
 */
@Singleton
public class WorkspaceStylesheet {
    private static final String HREF = "css/workspace.css";
    private static final String MARKER_ID = "handbook-workspace-stylesheet";

    @Inject WorkspaceStylesheet() {
        if (DomGlobal.document.getElementById(MARKER_ID) != null) return;
        HTMLLinkElement link = (HTMLLinkElement) DomGlobal.document.createElement("link");
        link.id = MARKER_ID;
        link.rel = "stylesheet";
        link.href = HREF;
        DomGlobal.document.head.appendChild(link);
    }
}
