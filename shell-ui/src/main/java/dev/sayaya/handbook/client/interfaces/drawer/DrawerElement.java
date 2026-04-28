package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.domain.DrawerState;
import dev.sayaya.handbook.client.interfaces.ShellStylesheet;
import dev.sayaya.handbook.client.usecase.DrawerMode;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.nav;

/**
 * 좌측 Drawer 네비게이션 컨테이너.
 *
 * <p><b>책임:</b> {@link DrawerMode} 상태(EXPAND/COLLAPSE/HIDE/OVERLAY) 에 따라 Drawer 의
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
 *   <li>{@link DrawerMode} — Drawer 상태 구독</li>
 *   <li>{@link MenuRailElement} / {@link ToolRailElement} — rail 자식</li>
 *   <li>{@link MenuToggleButton} — drawer 상단 햄버거 (rail 과 독립)</li>
 *   <li>{@link ShellStylesheet} — 생성자 주입으로 shell.css 로드 강제</li>
 * </ul></p>
 *
 * <p><b>비고 (2026-04):</b> 이전에 있던 엣지 스와이프 제스처(왼쪽 가장자리에서 오른쪽으로
 * 스와이프하여 OVERLAY 로 열기) 는 모바일에서 MenuRail 이 {@code display:none} 으로 숨어
 * 있고 MobileTabs 가 네비게이션을 대체하면서 OVERLAY 진입 동기가 소실되었다. swipe 관련
 * 필드·리스너·상수는 전부 제거되었고, OVERLAY 상태 자체는 상태 머신({@link DrawerState})
 * 에는 보존되어 있어 향후 명시적 Drawer 트리거(햄버거 재도입 등) 시 복원 가능하다.</p>
 */
@Singleton
public class DrawerElement implements IsElement<HTMLElement> {
    @Delegate private final HTMLContainerBuilder<HTMLElement> _this = nav();
    private final HTMLDivElement scrim;

    @Inject DrawerElement(DrawerMode mode, MenuRailElement navMenu, ToolRailElement navTools,
                          MenuToggleButton navToggle, ShellStylesheet shellStylesheet) {
        // shellStylesheet 는 생성자 주입만으로 shell.css 를 document.head 에 붙인다.
        // DrawerElement 가 shell-ui 의 UI 엔트리이므로 여기서 의존성을 강제하면 추가 부트스트랩 없이 자동 로드.
        assert shellStylesheet != null;
        scrim = div().css("drawer-scrim").element();
        scrim.addEventListener("click", e -> mode.toggleOverlay());

        // AppBar / MobileTabs 는 viewport 최상단 fixed 요소로서 body 직속에 배치된다 —
        // {@link dev.sayaya.handbook.client.ShellInitializer} 가 Composition Root 에서
        // 명시적 순서로 append. DrawerElement 는 rail 본체 + 햄버거 토글을 담당한다.
        // 햄버거는 drawer 의 첫 자식으로 둬 rail[hide] 와 독립적으로 유지 — rail 이 width:0 으로
        // 잘려도 drawer 자체가 visible 이면 햄버거는 노출되어야 하기 때문.
        _this.css("drawer")
                .add(navToggle)
                .add(div().css("body")
                        .add(navMenu).add(navTools));
        mode.subscribe(this::state);
    }

    private void state(DrawerState state) {
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
