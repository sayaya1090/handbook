package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.usecase.ToolSelected;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

public class ToolRailItemElement extends NavigationRailItemElement {
    private final HTMLContainerBuilder<HTMLDivElement> headline = div();
    private final Tool tool;
    @AssistedInject ToolRailItemElement(@Assisted Tool tool, ToolSelected selected, BehaviorSubject<Label> labels) {
        this.tool = tool;
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon()))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon()))
                .headline(headline.element());
        labels.subscribe(this::update);
        initEventHandlers(tool, selected);
        selected.subscribe(select->select(tool.equals(select)));
    }
    private void update(Label label) {
        headline.element().innerHTML = findLabelOrDefault(label, tool.title());
    }
    private String findLabelOrDefault(Label label, String key) {
        if(label==null) return key;
        return Js.asPropertyMap(label).has(key) ? Js.asPropertyMap(label).get(key).toString() : key;
    }
    private void initEventHandlers(Tool tool, ToolSelected selected) {
        on(EventType.click, evt-> select(tool, selected));
    }
    private void select(Tool tool, ToolSelected selected) {
        selected.next(tool);
    }
}
