package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.ui.elements.CardElementBuilder;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import org.jboss.elemento.ElementEventMethods;
import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.CardElementBuilder.card;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.label;

/*
  mode에 따라 다르게 출력한다.
    SIMPLE: 이름만 출력
    DETAIL: 이름, 설명, 속성 출력
 */
public class BoxUi {
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private final CardElementBuilder<?, ?> card;
    private final HTMLContainerBuilder<HTMLLabelElement> title;
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder btnAdd;
    public BoxUi() {
        this.container = div().css("type-box").attr("tabindex", "0");
        this.card = card().outlined().css("card");
        this.title = label().css("label");
        this.btnAdd = button().icon().add(icon("add")).css("add");

        container.add(card.add(title).add(btnAdd));
    }

    public void update(Box box) {
        title.text(box.name());
        container.element().style.left = box.x() + "px";
        container.element().style.top = box.y() + "px";
        card.element().style.width = CSSProperties.WidthUnionType.of(box.width() + "px");
        card.element().style.height = CSSProperties.HeightUnionType.of(box.height() + "px");
    }
    public void setMode(BoxDisplayState mode) {
        if (mode == BoxDisplayState.SIMPLE) {
            container.attr("simple", true);
            container.element().removeAttribute("detail");
        } else if (mode == BoxDisplayState.DETAIL) {
            container.attr("detail", true);
            container.element().removeAttribute("simple");
        }
    }

    public void setSelected(boolean isSelected) {
        if (isSelected) container.element().setAttribute("selected", "");
        else container.element().removeAttribute("selected");
    }
    public HTMLContainerBuilder<HTMLDivElement> getContainerElement() {
        return container;
    }
    private <V extends elemental2.dom.Event> void attachHandler(ElementEventMethods<?, ?> elem, EventType<V, ?> eventType, EventCallbackFn<V> callback) {
        elem.on(eventType, callback);
    }
    public <V extends elemental2.dom.Event> void attachAddButtonHandler(EventType<V, ?> eventType, EventCallbackFn<V> callback) {
        attachHandler(btnAdd, eventType, callback);
    }
    public <V extends elemental2.dom.Event> void attachTitleHandler(EventType<V, ?> eventType, EventCallbackFn<V> callback) {
        attachHandler(title, eventType, callback);
    }

}
