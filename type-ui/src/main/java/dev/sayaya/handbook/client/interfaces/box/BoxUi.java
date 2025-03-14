package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.handbook.client.domain.value.ValueListElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.rx.subject.Subject;
import dev.sayaya.ui.elements.CardElementBuilder;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import org.jboss.elemento.*;

import java.util.List;

import static dev.sayaya.rx.subject.Subject.subject;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.CardElementBuilder.card;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
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
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder title;
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder btnAdd;
    private final Subject<List<Value>> values = (Subject) subject(List.class);
    public BoxUi() {
        this.container = div().css("type-box");
        this.card = card().outlined().css("card").attr("tabindex", "0");
        this.title = textField().outlined().css("label");
        this.btnAdd = button().icon().add(icon("add")).css("add");
        container.add(card.add(title)
                .add(new ValueListElement(this.values))
                .add(btnAdd));
    }

    public void update(Box box) {
        title.value(box.name());
        container.element().style.left = box.x() + "px";
        container.element().style.top = box.y() + "px";
        card.element().style.width = CSSProperties.WidthUnionType.of(box.width() + "px");
        card.element().style.height = CSSProperties.HeightUnionType.of(box.height() + "px");
        values.next(box.values());

        /*DomGlobal.setTimeout(e->{
            box.height((int)card.element().getBoundingClientRect().height);
            card.element().style.height = CSSProperties.HeightUnionType.of(box.height() + "px");
        }, 100);*/
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
