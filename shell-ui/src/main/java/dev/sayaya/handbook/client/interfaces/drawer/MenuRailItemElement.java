package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

public class MenuRailItemElement extends NavigationRailItemElement {
    private final HTMLContainerBuilder<HTMLDivElement> headline = div();
    private final HTMLContainerBuilder<HTMLDivElement> supportingText = div();
    private final Menu menu;
    @AssistedInject MenuRailItemElement(@Assisted Menu menu, MenuSelected selected, MenuHover hover, MenuHoverElementProvider hoverElement, BehaviorSubject<Label> labels) {
        this.menu = menu;
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon()))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", menu.icon()))
                .headline(headline.element()).supportingText(supportingText.element());
        labels.subscribe(this::update);
        if(menu.tools() !=null && menu.tools().length > 1) trailingSupportingText("▶");
        initEventHandlers(menu, selected, hover, hoverElement);
        selected.subscribe(select->select(menu.equals(select)));
    }
    private void update(Label label) {
        printLabelOrDefault(label, menu.title(), headline.element());
        printLabelOrDefault(label, menu.supportingText(), supportingText.element());
    }
    private void printLabelOrDefault(Label label, String key, HTMLElement element) {
        String labelText = findLabelOrDefault(label, key);
        if(labelText!=null) element.innerHTML = labelText;
        else element.innerHTML = key;
    }
    private String findLabelOrDefault(Label label, String key) {
        if(label==null) return key;
        return Js.asPropertyMap(label).has(key) ? Js.asPropertyMap(label).get(key).toString() : key;
    }
    private void initEventHandlers(Menu menu, MenuSelected selected, MenuHover hover, MenuHoverElementProvider hoverElement) {
        on(EventType.click, evt-> select(menu, selected));
        on(EventType.mouseover, evt->{
            if(hover.getValue() == menu) return;
            hover.next(menu);
            hoverElement.next(this);
        });
    }
    private void select(Menu menu, MenuSelected selected) {
        selected.next(menu);
    }
}