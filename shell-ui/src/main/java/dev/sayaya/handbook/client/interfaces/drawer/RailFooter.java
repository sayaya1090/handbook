package dev.sayaya.handbook.client.interfaces.drawer;

import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * Drawer Rail 의 하단에 고정되는 슬롯 컨테이너.
 *
 * <p><b>책임:</b> 테마 토글, 햄버거 메뉴 토글, (장래의) 로그아웃 등 UI 컨트롤 버튼을
 * MenuRail 하단에 조립 순서에 따라 배치한다. Rail 자체가 얇게 유지될 수 있도록
 * 세로 방향으로 스택된다.</p>
 *
 * <p><b>조립 규칙:</b> Shell 은 특정 버튼의 존재를 가정하지 않는다. 각 모듈이 자신의
 * 버튼을 window CustomEvent {@code handbook-rail-footer-register} 로 발행하면
 * (또는 같은 모듈 내부에서 {@link #register} 로 직접 호출), RailFooter 가 버튼의
 * {@code order} CSS 속성을 부여해 flex 컨테이너 안에 삽입한다. 값이 작을수록
 * 위쪽에 배치된다. 표준 우선순위:</p>
 * <ul>
 *   <li>10  — 테마 토글 (shell-ui 가 등록)</li>
 *   <li>20  — 로그아웃 (login-ui 가 로드 시점에 등록)</li>
 *   <li>100 — 햄버거 메뉴 토글 (shell-ui 가 등록, 항상 최하단)</li>
 * </ul>
 *
 * <p><b>CustomEvent detail 스키마:</b>
 * <pre>{ element: HTMLElement, order: number }</pre></p>
 *
 * <p><b>의존관계 방향:</b> 다른 모듈은 shell-ui 의 이 클래스를 직접 참조하지 않는다 —
 * CustomEvent 이름과 detail 스키마만 계약으로 공유한다. 덕분에 shell 은 로그아웃 버튼을,
 * login-ui 는 테마 버튼을 서로 알 필요가 없다.</p>
 */
@Singleton
public class RailFooter implements IsElement<HTMLElement> {
    public static final String EVENT_NAME = "handbook-rail-footer-register";
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("rail-footer");

    @Inject RailFooter() {
        DomGlobal.window.addEventListener(EVENT_NAME, evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            Object detail = ce.detail;
            if (detail == null) return;
            JsPropertyMap<Object> map = Js.cast(detail);
            HTMLElement el = Js.cast(map.get("element"));
            if (el == null) return;
            Object orderObj = map.get("order");
            int order = orderObj instanceof Number ? ((Number) orderObj).intValue() : 50;
            register(el, order);
        });
    }

    /**
     * 같은 모듈(shell-ui) 내부 컴포넌트가 라운드트립 없이 직접 등록할 때 사용.
     * 외부 모듈은 CustomEvent 로 등록해 shell-ui 에 대한 컴파일 시점 의존성을 피한다.
     */
    public void register(HTMLElement el, int order) {
        el.style.setProperty("order", String.valueOf(order));
        _this.element().appendChild(el);
    }
}
