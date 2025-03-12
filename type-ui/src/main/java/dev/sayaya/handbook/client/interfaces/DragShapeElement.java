package dev.sayaya.handbook.client.interfaces;

import com.google.gwt.event.shared.HandlerRegistration;
import dev.sayaya.handbook.client.usecase.DropEventHandler;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import elemental2.dom.*;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.elemento.Elements.div;

@Singleton
public class DragShapeElement extends HTMLContainerBuilder<HTMLDivElement> {
    @Inject DragShapeElement() {
        this(div());
    }
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private DragShapeElement(HTMLContainerBuilder<HTMLDivElement> container) {
        super(container.element());
        this.container = container;
        container.attr("draggable", true).css("drag-shape").style("""
            display: none;
            position: absolute;
            border: 3px dotted #A9A9A9;
            z-index: 100;
           """);
        on(EventType.dragstart, this::dragStartEventHandler);
        on(EventType.drag, this::dragEventHandler);
        on(EventType.dragend, this::dropEventHandler);
        on(EventType.mouseup, evt->hide());
    }
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private BoxElement targetElement;
    public void delegateDragAndDropHandler(BoxElement boxElement) {
        boxElement.on(EventType.mousedown, evt-> triggerDragEvent(evt, boxElement));
    }
    private void triggerDragEvent(MouseEvent evt, BoxElement targetElement) {
        this.targetElement = targetElement;
        var param = DragEventInit.create();
        param.setBubbles(true);
        param.setCancelable(true);
        var dragEvent = new DragEvent("dragstart", param);
        element().dispatchEvent(dragEvent);
    }
    private void dragStartEventHandler(MouseEvent evt) {
        visible();
        dragOffsetX = (int) (evt.clientX - targetElement.element().offsetLeft);
        dragOffsetY = (int) (evt.clientY - targetElement.element().offsetTop);
    }
    private void dragEventHandler(MouseEvent evt) {
        evt.preventDefault();
        evt.stopPropagation();
        move(evt);
    }
    private void dropEventHandler(MouseEvent evt) {
        evt.preventDefault();
        evt.stopPropagation();
        hide();
        int newX = (int) (evt.clientX - dragOffsetX);
        int newY = (int) (evt.clientY - dragOffsetY);
        int deltaX = newX - targetElement.element().offsetLeft;
        int deltaY = newY - targetElement.element().offsetTop;
        for(var handler: handlers) handler.onInvoke(targetElement, deltaX, deltaY);
        targetElement = null;
    }
    private void visible() {
        container.element().style.display = "block";
        container.element().style.left = (targetElement.element().offsetLeft-2) + "px";
        container.element().style.top = (targetElement.element().offsetTop-2) + "px";
        container.element().style.width = CSSProperties.WidthUnionType.of(targetElement.element().offsetWidth + "px");
        container.element().style.height = CSSProperties.HeightUnionType.of(targetElement.element().offsetHeight + "px");
    }
    private void move(MouseEvent evt) {
        int newX = (int) (evt.clientX - dragOffsetX -2);
        int newY = (int) (evt.clientY - dragOffsetY -2);
        container.element().style.left = newX + "px";
        container.element().style.top = newY + "px";
    }
    private void hide() {
        container.element().style.display = "none";
    }
    private final Set<DropEventHandler> handlers = new HashSet<>();
    public HandlerRegistration on(DropEventHandler handler) {
        handlers.add(handler);
        return ()->handlers.remove(handler);
    }
}
