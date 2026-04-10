package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.action.ChangeLayoutAction;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * 이전 레이아웃 기간으로 이동하는 탐색 버튼.
 *
 * <p><b>책임:</b> 클릭 시 현재 기간의 이전 기간으로 {@link ChangeLayoutAction}을 실행한다.
 * 첫 번째 기간이면 비활성화(disabled)된다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link LayoutProvider} — 현재 선택된 기간</li>
 *   <li>{@link LayoutList} — 전체 기간 목록</li>
 *   <li>{@link ActionManager} — ChangeLayoutAction 실행</li>
 * </ul></p>
 * <p><b>주의:</b> 기간 목록이나 현재 선택이 변경되면 disabled 상태를 자동 갱신한다.</p>
 */
@Singleton
public class BeforeButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    BeforeButton(LayoutProvider layoutProvider, LayoutList layoutList, ActionManager actionManager) {
        root = ButtonElementBuilder.button().text()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-chevron-left"))
                .css("type-ctrl-btn").css("type-ctrl-btn-before")
                .element();

        root.addEventListener("click", e -> navigate(layoutProvider, layoutList, actionManager, -1));

        layoutList.subscribe(periods -> updateEnabled(layoutProvider, periods));
        layoutProvider.subscribe(period -> updateEnabled(layoutProvider, layoutList.getValue()));
    }

    private void navigate(LayoutProvider provider, LayoutList layoutList, ActionManager actionManager, int direction) {
        List<LayoutPeriod> periods = layoutList.getValue();
        LayoutPeriod current = provider.getValue();
        if (current == null || periods.isEmpty()) return;
        int idx = periods.indexOf(current);
        int target = idx + direction;
        if (target >= 0 && target < periods.size()) {
            actionManager.execute(new ChangeLayoutAction(provider, current, periods.get(target)));
        }
    }

    private void updateEnabled(LayoutProvider provider, List<LayoutPeriod> periods) {
        LayoutPeriod current = provider.getValue();
        if (current == null || periods.isEmpty()) {
            root.toggleAttribute("disabled", true);
            return;
        }
        int idx = periods.indexOf(current);
        root.toggleAttribute("disabled", idx <= 0);
    }

    @Override
    public HTMLElement element() { return root; }
}
