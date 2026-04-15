package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.UserPreferences;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 라이트/다크 테마 전환 토글 버튼.
 *
 * <p><b>책임:</b> {@code <html>} 의 {@code color-theme} 속성을 light/dark 로 토글한다.
 * 초기 테마는 UserPreferences(localStorage)에서 불러오며, 없으면 OS 의 prefers-color-scheme 을
 * 따른다.</p>
 *
 * <p><b>구조:</b> {@link NavigationRailItemElement} 를 상속해 일반 메뉴 아이템과 동일한
 * .item > (.collapse + .expand) 구조를 가진다. collapse 모드에서는 아이콘 버튼만, expand
 * 모드에서는 md-item 의 headline 라벨까지 노출되어 다른 메뉴와 시각적 일관성을 유지한다.</p>
 *
 * <p><b>아이콘 애니메이션:</b> .collapse 와 md-item 의 slot=start 두 곳 모두에 sun/moon
 * SVG 를 동시 렌더링한다. CSS 의 {@code :root[color-theme]} 셀렉터로 opacity/transform
 * 트랜지션을 걸어 일출/일몰처럼 점진적으로 전환되며, 두 위치에서 독립적으로 동기화된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link UserPreferences} — 테마 설정 읽기/저장 (localStorage)</li>
 *   <li>{@link LabelProvider} — i18n 라벨. 현재 모드에 따라 두 키 중 하나 사용:
 *     {@code theme.switch_to_dark} (현재 light), {@code theme.switch_to_light} (현재 dark).
 *     darkMode 토글이나 locale 변경 시 라벨이 자동 갱신된다.</li>
 * </ul></p>
 */
@Singleton
public class ThemeToggle extends NavigationRailItemElement {
    private static final String SVG_NS = "http://www.w3.org/2000/svg";
    private static final String ICON_SUN = "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5 5-2.24,5-5-2.24-5-5-5zM2,13H4V11H2Zm18,0h2V11H20ZM11,2V4h2V2Zm0,18v2h2V20ZM5.99,4.58 4.58,5.99 6.34,7.76 7.76,6.34ZM18.36,17.64l-1.41,1.41 1.76,1.77 1.41-1.41ZM18.36,6.34l1.77-1.76-1.41-1.41-1.76,1.77ZM6.34,17.64l-1.76,1.77 1.41,1.41 1.76-1.77Z";
    private static final String ICON_MOON = "M12,3c-4.97,0-9,4.03-9,9s4.03,9,9,9 9-4.03,9-9c0-.46-.04-.92-.1-1.36-.98,1.37-2.58,2.26-4.4,2.26-2.98,0-5.4-2.42-5.4-5.4 0-1.82,.89-3.42,2.26-4.4-.44-.06-.9-.1-1.36-.1Z";

    private static final String LABEL_KEY_TO_DARK = "theme.switch_to_dark";
    private static final String LABEL_KEY_TO_LIGHT = "theme.switch_to_light";
    private static final String DEFAULT_TO_DARK = "Switch to Dark";
    private static final String DEFAULT_TO_LIGHT = "Switch to Light";

    private boolean darkMode;
    private final HTMLElement headlineEl = div().element();
    private Labels currentLabels;

    @Inject ThemeToggle(LabelProvider labelProvider) {
        // .collapse 와 md-item 의 slot=start 두 곳에 각자 독립된 SVG 를 추가.
        // 두 SVG 는 .theme-toggle-svg 클래스를 공유하므로 :root[color-theme] CSS 로 동일하게 동기화된다.
        this.icon(createThemeSvg()).start(createThemeSvg()).headline(headlineEl);
        element().classList.add("rail-bottom");
        labelProvider.subscribe(labels -> {
            currentLabels = labels;
            updateHeadline();
        });

        String saved = UserPreferences.getTheme();
        if (saved != null) {
            darkMode = "dark".equals(saved);
        } else {
            darkMode = detectSystemDarkMode();
        }
        applyTheme();
        updateHeadline();
        on(EventType.click, evt -> toggle());
    }

    /** 시스템이 다크 모드인지 matchMedia를 통해 감지한다. */
    private static boolean detectSystemDarkMode() {
        return DomGlobal.window.matchMedia("(prefers-color-scheme: dark)").matches;
    }

    private void toggle() {
        darkMode = !darkMode;
        applyTheme();
        updateHeadline();
        // 토글 순간에만 :root 에 theme-changing 클래스를 잠깐 부착해 일출/일몰 keyframe
        // 애니메이션을 트리거. 500ms 후 자동 제거되어 다른 DOM 변화(예: drawer expand 로
        // md-item 의 start svg 가 display:none → visible 로 바뀌는 순간) 에서는
        // animation-name 이 매칭되지 않아 애니메이션이 재생되지 않는다.
        var html = DomGlobal.document.documentElement;
        html.classList.add("theme-changing");
        DomGlobal.setTimeout(args -> html.classList.remove("theme-changing"), 500);
        UserPreferences.setTheme(darkMode ? "dark" : "light");
    }

    /**
     * 현재 darkMode 상태에 맞춰 headline 라벨을 갱신한다. light 일 때는 "다크로 전환",
     * dark 일 때는 "라이트로 전환" — 즉 클릭 시 전환될 다음 모드를 안내한다.
     * LabelProvider 가 아직 로드되지 않은 시점(currentLabels == null)에서도 default 값을
     * 사용해 안전하게 렌더한다.
     */
    private void updateHeadline() {
        String key = darkMode ? LABEL_KEY_TO_LIGHT : LABEL_KEY_TO_DARK;
        String fallback = darkMode ? DEFAULT_TO_LIGHT : DEFAULT_TO_DARK;
        String text = (currentLabels != null) ? currentLabels.getOrDefault(key, fallback) : fallback;
        headlineEl.innerHTML = text.toUpperCase();
    }

    private void applyTheme() {
        HTMLElement html = DomGlobal.document.documentElement;
        html.setAttribute("color-theme", darkMode ? "dark" : "light");
    }

    /**
     * sun + moon path 두 개를 갖는 SVG 를 생성한다. 가시성/위치 전환은 shell.css 의 @keyframes
     * (theme-icon-rise / theme-icon-set) 가 담당하며 color-theme 속성 변경 시 animation-name
     * 교체로 재생된다.
     */
    private Element createThemeSvg() {
        Element svg = DomGlobal.document.createElementNS(SVG_NS, "svg");
        svg.setAttribute("viewBox", "0 0 24 24");
        svg.setAttribute("width", "24");
        svg.setAttribute("height", "24");
        svg.setAttribute("class", "theme-toggle-svg icon");
        svg.appendChild(createPath(ICON_SUN, "sun"));
        svg.appendChild(createPath(ICON_MOON, "moon"));
        return svg;
    }

    private Element createPath(String d, String cssClass) {
        Element path = DomGlobal.document.createElementNS(SVG_NS, "path");
        path.setAttribute("d", d);
        path.setAttribute("fill", "currentColor");
        path.setAttribute("class", cssClass);
        return path;
    }
}
