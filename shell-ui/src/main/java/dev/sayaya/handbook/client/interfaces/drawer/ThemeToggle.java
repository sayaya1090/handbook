package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.usecase.UserPreferences;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

/**
 * 다크/라이트 테마 전환 토글 버튼.
 *
 * <p><b>책임:</b> html 요소에 color-theme 속성을 설정하여 다크/라이트 모드를 전환한다.
 * 초기 테마는 UserPreferences(localStorage)에서 불러오며, 전환 시 저장한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link UserPreferences} — 테마 설정 읽기/저장</li>
 * </ul></p>
 *
 * <p><b>주의:</b> CSS 규칙이 [color-theme="dark"] 셀렉터로 MD3 토큰을 오버라이드한다 (기존 컨벤션).</p>
 */
@Singleton
public class ThemeToggle implements IsElement<HTMLElement> {
    private static final String SVG_NS = "http://www.w3.org/2000/svg";
    private static final String ICON_LIGHT = "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5 5-2.24,5-5-2.24-5-5-5zM2,13H4V11H2Zm18,0h2V11H20ZM11,2V4h2V2Zm0,18v2h2V20ZM5.99,4.58 4.58,5.99 6.34,7.76 7.76,6.34ZM18.36,17.64l-1.41,1.41 1.76,1.77 1.41-1.41ZM18.36,6.34l1.77-1.76-1.41-1.41-1.76,1.77ZM6.34,17.64l-1.76,1.77 1.41,1.41 1.76-1.77Z";
    private static final String ICON_DARK = "M12,3c-4.97,0-9,4.03-9,9s4.03,9,9,9 9-4.03,9-9c0-.46-.04-.92-.1-1.36-.98,1.37-2.58,2.26-4.4,2.26-2.98,0-5.4-2.42-5.4-5.4 0-1.82,.89-3.42,2.26-4.4-.44-.06-.9-.1-1.36-.1Z";

    @Delegate private final IconButtonElementBuilder.PlainIconButtonElementBuilder icon;
    private boolean darkMode;

    @Inject ThemeToggle() {
        icon = new IconButtonElementBuilder.PlainIconButtonElementBuilder();
        String saved = UserPreferences.getTheme();
        if (saved != null) {
            darkMode = "dark".equals(saved);
        } else {
            darkMode = detectSystemDarkMode();
        }
        applyTheme();
        updateIcon();
        on(EventType.click, evt -> toggle());
    }

    /** 시스템이 다크 모드인지 matchMedia를 통해 감지한다. */
    private static boolean detectSystemDarkMode() {
        return DomGlobal.window.matchMedia("(prefers-color-scheme: dark)").matches;
    }

    private void toggle() {
        darkMode = !darkMode;
        applyTheme();
        updateIcon();
        UserPreferences.setTheme(darkMode ? "dark" : "light");
    }

    private void applyTheme() {
        HTMLElement html = DomGlobal.document.documentElement;
        html.setAttribute("color-theme", darkMode ? "dark" : "light");
    }

    private void updateIcon() {
        HTMLElement el = icon.element();
        // 기존 SVG 제거 후 새 아이콘 추가
        while (el.firstChild != null) {
            el.removeChild(el.firstChild);
        }
        el.appendChild(createSvg(darkMode ? ICON_LIGHT : ICON_DARK));
    }

    private Element createSvg(String pathD) {
        var svg = DomGlobal.document.createElementNS(SVG_NS, "svg");
        svg.setAttribute("viewBox", "0 0 24 24");
        svg.setAttribute("width", "24");
        svg.setAttribute("height", "24");
        var path = DomGlobal.document.createElementNS(SVG_NS, "path");
        path.setAttribute("d", pathD);
        path.setAttribute("fill", "currentColor");
        svg.appendChild(path);
        return svg;
    }
}
