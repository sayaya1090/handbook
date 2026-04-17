package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.interfaces.ShellStylesheet;
import dev.sayaya.handbook.client.usecase.DrawerMode;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.TouchEvent;
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
 * <p><b>책임:</b> DrawerMode의 상태(EXPAND/COLLAPSE/HIDE/OVERLAY)에 따라 Drawer를 전환한다.
 * 모바일(OVERLAY)에서는 position: fixed 오버레이 + 배경 스크림을 표시하며,
 * 화면 왼쪽 가장자리 스와이프로 열기/닫기를 지원한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DrawerMode} — Drawer 상태 구독</li>
 *   <li>{@link MenuToggleButton} — 햄버거 토글 버튼</li>
 *   <li>{@link MenuRailElement} — 메뉴 레일</li>
 *   <li>{@link ToolRailElement} — 도구 레일</li>
 *   <li>{@link WorkspaceSelectElement} — 워크스페이스 셀렉터</li>
 *   <li>{@link ThemeToggle} — 라이트/다크 테마 토글 (MenuRail 하단에 margin-top:auto 로 고정)</li>
 * </ul></p>
 *
 * <p><b>레이아웃:</b>
 * <ul>
 *   <li>header — 워크스페이스 셀렉터 + 햄버거 토글 가로 일렬 (justify-content: space-between)</li>
 *   <li>본문 — MenuRail + ToolRail 가로 배치</li>
 *   <li>ThemeToggle 은 MenuRail 의 마지막 자식으로 직접 추가되어 Menu 도메인의 {@code bottom=true}
 *       와 동일한 {@code margin-top: auto} 패턴으로 rail 하단에 고정. rail 의 width 트랜지션과
 *       자동으로 동기화되어 expand/collapse 전환 시 같이 움직인다.</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 스와이프 제스처는 왼쪽 가장자리 30px 이내에서 시작하고 60px 이상 이동 시 트리거된다.</p>
 */
@Singleton
public class DrawerElement implements IsElement<HTMLElement> {
    private static final int SWIPE_EDGE_THRESHOLD = 30;
    private static final int SWIPE_MIN_DISTANCE = 60;

    @Delegate private final HTMLContainerBuilder<HTMLElement> _this = nav();
    private final DrawerMode mode;
    private final HTMLDivElement scrim;
    private double touchStartX;
    private boolean trackingSwipe;

    @Inject DrawerElement(DrawerMode mode, MenuToggleButton btnToggle, MenuRailElement navMenu, MobileTabsElement navTabs, ToolRailElement navTools, WorkspaceSelectElement workspace, ThemeToggle themeToggle, ShellAppBarElement appBar, ShellStylesheet shellStylesheet) {
        this.mode = mode;
        // shellStylesheet 는 생성자 주입만으로 shell.css 를 document.head 에 붙인다.
        // DrawerElement 가 shell-ui 의 UI 엔트리이므로 여기서 의존성을 강제하면 추가 부트스트랩 없이 자동 로드.
        assert shellStylesheet != null;
        scrim = div().css("drawer-scrim").element();
        scrim.addEventListener("click", e -> mode.toggleOverlay());

        // AppBar 는 데스크톱/모바일 공통 상단 bar. 햄버거/워크스페이스/테마 토글을 항상 AppBar
        // slot 으로 배치하여 viewport 분기 없이 일관된 전역 액션 축을 유지한다. Drawer 는
        // AppBar 아래로 밀려서 MenuRail + ToolRail 본체만 담당한다.
        appBar.element().removeAttribute("hide");
        appBar.leadingSlot().appendChild(btnToggle.element());
        appBar.centerSlot().appendChild(workspace.css("workspace").element());
        appBar.trailingSlot().appendChild(themeToggle.element());

        // navTabs 는 모바일 상단 Scrollable Tabs — AppBar 바로 아래에 stack. 데스크톱에서는
        // [hide] 속성으로 자체 숨김 (MenuRail 이 네비게이션 담당).
        _this.css("drawer")
                .add(div().css("body")
                        .add(navMenu).add(navTools))
                .add(navTabs)
                .add(appBar);
        mode.subscribe(this::state);
        initSwipeGesture();
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

    private void initSwipeGesture() {
        DomGlobal.document.addEventListener("touchstart", e -> {
            TouchEvent te = (TouchEvent) e;
            if (te.touches.length > 0) {
                double x = te.touches.item(0).clientX;
                if (x < SWIPE_EDGE_THRESHOLD && mode.isMobile()) {
                    touchStartX = x;
                    trackingSwipe = true;
                } else if (mode.getValue() == DrawerState.OVERLAY) {
                    touchStartX = x;
                    trackingSwipe = true;
                }
            }
        });
        DomGlobal.document.addEventListener("touchend", e -> {
            if (!trackingSwipe) return;
            trackingSwipe = false;
            TouchEvent te = (TouchEvent) e;
            if (te.changedTouches.length > 0) {
                double endX = te.changedTouches.item(0).clientX;
                double delta = endX - touchStartX;
                if (delta > SWIPE_MIN_DISTANCE && mode.getValue() != DrawerState.OVERLAY) {
                    mode.toggleOverlay();
                } else if (delta < -SWIPE_MIN_DISTANCE && mode.getValue() == DrawerState.OVERLAY) {
                    mode.toggleOverlay();
                }
            }
        });
    }
}
