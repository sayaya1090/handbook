package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.DrawerState;
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
 *   <li>{@link ThemeToggle} — 테마 토글 버튼 (RailFooter 에 order=10 으로 등록)</li>
 *   <li>{@link RailFooter} — MenuRail 하단 슬롯 (조립 우선순위 기반 버튼 배치)</li>
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

    @Inject DrawerElement(DrawerMode mode, MenuToggleButton btnToggle, MenuRailElement navMenu, ToolRailElement navTools, WorkspaceSelectElement workspace, ThemeToggle themeToggle, RailFooter railFooter) {
        this.mode = mode;
        scrim = div().css("drawer-scrim").element();
        scrim.addEventListener("click", e -> mode.toggleOverlay());

        // 헤더에는 워크스페이스 셀렉터만 남는다. 테마 토글과 햄버거 토글은 RailFooter 로 이관되어
        // 조립 우선순위(order CSS) 기반으로 Rail 하단에 세로 스택된다.
        //   order=10  → 테마 토글
        //   order=100 → 햄버거 토글 (항상 최하단)
        //   order=20  → 향후 로그아웃 (login-ui 모듈이 CustomEvent 로 등록)
        railFooter.register(themeToggle.element(), 10);
        railFooter.register(btnToggle.element(), 100);
        // RailFooter 는 MenuRail flex-column 의 마지막 자식으로 추가되어 margin-top: auto 로 하단 고정.
        navMenu.element().appendChild(railFooter.element());

        _this.css("drawer")
                .add(div().css("header", "drawer-header")
                        .add(workspace.css("workspace")))
                .add(div().style("display: flex; flex: 1; min-height: 0;")
                        .add(navMenu).add(navTools));
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
