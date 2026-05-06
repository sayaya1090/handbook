package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.interfaces.ShellStylesheet;
import dev.sayaya.handbook.client.usecase.MenuHover;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.nav;

/**
 * 좌측 Drawer 네비게이션 컨테이너.
 *
 * <p><b>책임:</b> {@link dev.sayaya.handbook.client.usecase.DrawerMode} 상태(EXPAND/COLLAPSE/HIDE/OVERLAY) 에 따라 Drawer 의
 * {@code [open]/[hide]/[overlay]} 속성을 토글하고, 하단 MenuRail / ToolRail 을 자식으로 담는다.
 * OVERLAY 모드에서는 배경 스크림을 노출하며, 스크림 클릭으로 overlay 를 닫는다.</p>
 *
 * <p><b>햄버거 토글 배치(2026-04-18 ~ MenuRail → Drawer 직속 이관):</b>
 * {@link MenuToggleButton} 은 drawer flex-column 의 **첫 자식**(`.body` 컨테이너 앞)으로
 * 마운트된다. 이유는 "MenuRail 이 HIDE(width:0 + overflow:hidden) 여도 drawer 자체가
 * visible 인 동안에는 햄버거가 보여야" 하기 때문 — rail 의 자식이면 rail[hide] 에 걸려
 * 함께 잘리므로 독립 배치. drawer[hide] / drawer[overlay] 에서만 CSS 로 숨긴다.
 * 모바일에서는 `.drawer:has(.menu-rail[mobile])` 분기로 숨김 처리(MobileTabs 가 대체).</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link MenuRailElement} / {@link ToolRailElement} — rail 자식</li>
 *   <li>{@link MenuToggleButton} — drawer 상단 햄버거 (rail 과 독립)</li>
 *   <li>{@link ShellStylesheet} — 생성자 주입으로 shell.css 로드 강제</li>
 *   <li>{@link MenuHover} — 호버 터널링 상태 관리</li>
 * </ul></p>
 */
@Singleton
public class DrawerElement implements IsElement<HTMLElement> {
    @Delegate private final HTMLContainerBuilder<HTMLElement> _this = nav();
    private final HTMLDivElement scrim;

    @Inject DrawerElement(MenuRailElement navMenu, ToolRailElement navTools,
                          MenuToggleButton navToggle, ShellStylesheet shellStylesheet,
                          MenuHover hover) {
        // shellStylesheet 는 생성자 주입만으로 shell.css 를 document.head 에 붙인다.
        assert shellStylesheet != null;
        scrim = div().css("drawer-scrim").element();

        var body = div().css("body")
                .add(navMenu).add(navTools);
        
        // 2026-05-05: '호버 터널' 문제 해결. 
        // 메뉴 레일에서 마우스가 나가더라도 툴 레일(자식)로 이동 중이라면 호버를 유지해야 한다.
        // 따라서 두 레일을 감싸는 부모인 .body 에 mouseleave 핸들러를 두어, 
        // 드로어 영역 전체를 벗어날 때만 호버 상태를 초기화한다.
        body.on(EventType.mouseleave, e -> hover.next(null));

        _this.css("drawer")
                .add(navToggle)
                .add(body);
    }

    public void onOverlayClick(Runnable action) {
        scrim.addEventListener("click", e -> action.run());
    }

    public void setState(DrawerState state) {
        switch (state) {
            case EXPAND -> {
                element().setAttribute("open", true);
                element().removeAttribute("hide");
                element().removeAttribute("overlay");
                removeScrim();
            }
            case COLLAPSE -> {
                element().removeAttribute("open");
                element().removeAttribute("hide");
                element().removeAttribute("overlay");
                removeScrim();
            }
            case HIDE -> {
                element().removeAttribute("open");
                element().setAttribute("hide", true);
                element().removeAttribute("overlay");
                removeScrim();
            }
            case OVERLAY -> {
                element().removeAttribute("hide");
                element().setAttribute("overlay", true);
                element().setAttribute("open", true);
                showScrim();
            }
        }
    }

    private void showScrim() {
        if (element().parentElement != null && scrim.parentElement == null) {
            element().parentElement.appendChild(scrim);
        }
        DomGlobal.requestAnimationFrame(t -> scrim.setAttribute("visible", true));
    }

    private void removeScrim() {
        scrim.removeAttribute("visible");
        if (scrim.parentElement != null) {
            scrim.parentElement.removeChild(scrim);
        }
    }
}
