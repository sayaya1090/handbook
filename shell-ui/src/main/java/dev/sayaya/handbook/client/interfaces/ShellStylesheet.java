package dev.sayaya.handbook.client.interfaces;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLLinkElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Shell 전용 스타일시트({@code css/shell.css})를 런타임에 document.head 에 주입한다.
 *
 * <p><b>책임:</b> shell-ui 모듈이 자기 컴포넌트(Drawer, MenuRail, RailFooter …) 가
 * 의존하는 CSS 를 스스로 책임지고 로드한다. app 번들이 shell.css 의 존재를 미리
 * 알 필요가 없어지고, shell-ui 모듈은 자기 파일만 배포하면 완전히 독립적으로
 * 동작한다.</p>
 *
 * <p><b>조립 원칙:</b> 컴포넌트 단위 DOM 자산 주입. 같은 방식으로 다른 모듈
 * (login-ui, workspace-ui, …)도 자기 스타일시트/스크립트를 런타임에 직접 붙일
 * 수 있다. app.html 에는 공용 vendor 라이브러리와 aggregate 엔트리 포인트만 남는다.</p>
 *
 * <p><b>의존관계:</b> Dagger 가 @Singleton 으로 생성하며, {@link
 * dev.sayaya.handbook.client.interfaces.drawer.DrawerElement} 가 생성자 파라미터로
 * 받아 eager 하게 초기화되도록 유도한다. (DrawerElement 자체가 shell-ui 의 진입점
 * 컴포넌트이므로 여기에 의존성을 태우면 별도 부트스트랩 없이 바로 주입된다.)</p>
 */
@Singleton
public class ShellStylesheet {
    private static final String HREF = "css/shell.css";
    private static final String MARKER_ID = "handbook-shell-stylesheet";

    @Inject ShellStylesheet() {
        // 이미 주입되어 있으면 중복 등록 방지 (GWT devMode 리로드 / HMR 대응)
        if (DomGlobal.document.getElementById(MARKER_ID) != null) return;
        HTMLLinkElement link = (HTMLLinkElement) DomGlobal.document.createElement("link");
        link.id = MARKER_ID;
        link.rel = "stylesheet";
        link.href = HREF;
        DomGlobal.document.head.appendChild(link);
    }
}
