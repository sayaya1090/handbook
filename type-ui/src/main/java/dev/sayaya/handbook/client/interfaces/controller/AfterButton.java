package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

/** 
 * 다음 레이아웃 기간으로 이동하는 버튼.
 */
@Singleton
public class AfterButton implements IsElement<HTMLElement> {
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder _this;

    @Inject
    AfterButton(LayoutProvider layoutProvider, LayoutList layoutList, ActionManager actionManager) {
        _this = button().icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-chevron-right"))
                .css("type-ctrl-btn", "type-ctrl-btn-after");

        _this.onClick(e -> navigate(layoutProvider, layoutList, actionManager, 1));
        
        layoutProvider.subscribe(period -> updateEnabled(layoutProvider, layoutList.getValue()));
        layoutList.subscribe(periods -> updateEnabled(layoutProvider, periods));
    }

    private void updateEnabled(LayoutProvider provider, List<TypeLayout> layouts) {
        TypeLayout current = provider.getValue();
        if (current == null || layouts == null || layouts.isEmpty()) {
            _this.element().setAttribute("disabled", "true");
            return;
        }
        int index = layouts.indexOf(current);
        if (index >= 0 && index < layouts.size() - 1) {
            _this.element().removeAttribute("disabled");
        } else {
            _this.element().setAttribute("disabled", "true");
        }
    }

    private void navigate(LayoutProvider provider, LayoutList layoutList, ActionManager actionManager, int direction) {
        List<TypeLayout> layouts = layoutList.getValue();
        TypeLayout current = provider.getValue();
        int index = layouts.indexOf(current);
        int nextIndex = index + direction;
        if (nextIndex >= 0 && nextIndex < layouts.size()) {
            provider.replace(layouts.get(nextIndex));
        }
    }

    @Override
    public HTMLElement element() { return _this.element(); }
}
