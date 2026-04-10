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

@Singleton
public class BeforeButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    BeforeButton(LayoutProvider layoutProvider, LayoutList layoutList, ActionManager actionManager) {
        root = ButtonElementBuilder.button().text()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-chevron-left"))
                .css("type-ctrl-btn")
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
