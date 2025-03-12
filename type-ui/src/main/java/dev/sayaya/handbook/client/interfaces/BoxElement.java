package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import dev.sayaya.ui.elements.CardElementBuilder;
import elemental2.dom.*;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.sayaya.ui.elements.CardElementBuilder.card;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.label;

/*
  mode에 따라 다르게 출력한다.
    SIMPLE: 이름만 출력
    DETAIL: 이름, 설명, 속성 출력
 */
public class BoxElement extends HTMLContainerBuilder<HTMLDivElement> implements UpdatableBox {
    private static final Map<Box, BoxElement> cache = new ConcurrentHashMap<>(); // 생성된 Box 재사용
    public static BoxElement of(Box box, SelectedBoxElement selected, DragShapeElement dragShapeElement, dev.sayaya.handbook.client.interfaces.BoxDisplayMode mode) {
        return cache.computeIfAbsent(box, b -> new BoxElement(box, selected, dragShapeElement, mode));
    }

    private BoxElement(Box box, SelectedBoxElement selected, DragShapeElement dragShapeElement, dev.sayaya.handbook.client.interfaces.BoxDisplayMode mode) {
        this(div(), box, selected, dragShapeElement, mode);
    }
    private final Box box;
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private final CardElementBuilder<?, ?> card;
    private final HTMLContainerBuilder<HTMLLabelElement> title = label().css("label");
    private double dragStartTimer;
    private BoxElement(HTMLContainerBuilder<HTMLDivElement> container, Box box, SelectedBoxElement selected, DragShapeElement dragShapeElement, dev.sayaya.handbook.client.interfaces.BoxDisplayMode mode) {
        super(container.element());
        if (box == null) throw new IllegalArgumentException("Box must not be null.");
        this.container = container;
        this.box = box;
        this.card = card().outlined().css("card");
        container.css("type-box")
                 .add(card.add(title));
        update();
        mode.subscribe(this::changeMode);
        selected.subscribe(selectedBox -> select(selectedBox == this) );
        on(EventType.click, evt->{
            evt.stopPropagation();
            selected.next(this);
        });
        on(EventType.contextmenu, evt->{
            selected.next(this);
        });
        on(EventType.mousedown, evt-> {
            dragStartTimer = DomGlobal.setTimeout(v->{
                selected.next(this);
                dragShapeElement.triggerDragEvent();
            }, 500);
        });
        on(EventType.mouseup, evt->{
            if(dragStartTimer != 0) DomGlobal.clearTimeout(dragStartTimer);
        });
    }
    private void changeMode(BoxDisplayState mode) {
        if(mode == BoxDisplayState.SIMPLE) {
            container.attr("simple", true);
            container.element().removeAttribute("detail");
        } else if(mode == BoxDisplayState.DETAIL) {
            container.attr("detail", true);
            container.element().removeAttribute("simple");
        }
    }
    private void select(boolean isSelected) {
        if(isSelected) container.element().setAttribute("selected", "");
        else container.element().removeAttribute("selected");
    }
    @Override
    public Box box() {
        return box;
    }
    @Override
    public void update() {
        title.text(box.name());
        container.element().style.left = box.x() + "px";
        container.element().style.top = box.y() + "px";
        card.element().style.width = CSSProperties.WidthUnionType.of(box.width() + "px");
        card.element().style.height = CSSProperties.HeightUnionType.of(box.height() + "px");
    }
}
