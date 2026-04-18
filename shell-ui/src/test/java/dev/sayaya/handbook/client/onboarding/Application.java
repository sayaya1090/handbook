package dev.sayaya.handbook.client.onboarding;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Menu;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;

import java.util.List;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;

/**
 * UC-S21 테스트 EntryPoint.
 *
 * <p><b>책임:</b> WorkspaceOnboardingBootstrapper 를 초기화하고 MenuSelected 상태를 DOM 에 반영해
 * Playwright 가 push 발생 여부 / Menu 필드 / push 횟수를 관찰할 수 있게 한다.</p>
 *
 * <p><b>관찰 포인트:</b>
 * <ul>
 *   <li>{@code #selected-menu-title} — 최신 MenuSelected 의 title</li>
 *   <li>{@code #selected-menu-script} — 최신 Menu 의 script</li>
 *   <li>{@code #selected-menu-icon} — 최신 Menu 의 icon</li>
 *   <li>{@code #push-count} — MenuSelected 가 non-null 로 방출된 누적 횟수</li>
 *   <li>{@code #emit-empty} 버튼 — 빈 workspace list 를 upstream 으로 push</li>
 *   <li>{@code #emit-non-empty} 버튼 — workspace 1개가 담긴 list 를 push</li>
 * </ul></p>
 */
public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    private int pushCount = 0;

    @Override
    public void onModuleLoad() {
        HTMLElement title = div().id("selected-menu-title").element();
        HTMLElement script = div().id("selected-menu-script").element();
        HTMLElement icon = div().id("selected-menu-icon").element();
        HTMLElement iconType = div().id("selected-menu-icon-type").element();
        HTMLElement order = div().id("selected-menu-order").element();
        HTMLElement count = div().id("push-count").text("0").element();

        components.menuSelected().subscribe((Menu m) -> {
            if (m == null) return;
            pushCount++;
            title.textContent = m.title() != null ? m.title() : "";
            script.textContent = m.script() != null ? m.script() : "";
            icon.textContent = m.icon() != null ? m.icon() : "";
            iconType.textContent = m.iconType() != null ? m.iconType() : "";
            order.textContent = m.order() != null ? m.order() : "";
            count.textContent = String.valueOf(pushCount);
        });

        // Dagger 생성 시점에 Bootstrapper 의 구독 트리가 이미 설정되지만, WorkspaceList 가 보는
        // 초기값(List.of()) 이 empty 이므로 onModuleLoad 시점에 onboarding push 가 이미 발생.
        components.bootstrapper().initialize();

        body().add(div().id("onboarding-test-root")
                .add(title).add(script).add(icon).add(iconType).add(order).add(count)
                .add(button("emit-empty").id("emit-empty")
                        .on(EventType.click, e -> components.workspaceSubject().next(List.of())))
                .add(button("emit-non-empty").id("emit-non-empty")
                        .on(EventType.click, e -> components.workspaceSubject().next(
                                List.of(OnboardingMock.workspace("ws-1", "Test WS")))))
        );
    }
}
