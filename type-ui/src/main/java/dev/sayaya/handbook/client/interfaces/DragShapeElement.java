package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.ActionManager;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.MouseEvent;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        container.css("drag-shape").style("""
            display: none;
            position: absolute;
            border: 3px dotted #A9A9A9;
            z-index: 100;
           """);
        on(EventType.mousemove, this::dragEventHandler);
        on(EventType.mouseup, this::dropEventHandler);
    }
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private BoxElement targetElement;
    public void addDragAndDropHandler(BoxElement boxElement) {
        boxElement.on(EventType.mousedown, evt->dragStartEventHandler(evt, boxElement));
    }
    private void dragStartEventHandler(MouseEvent evt, BoxElement targetElement) {
        evt.preventDefault();
        evt.stopPropagation();
        this.targetElement = targetElement;
        visible();
        dragOffsetX = (int) (evt.clientX - targetElement.element().offsetLeft);
        dragOffsetY = (int) (evt.clientY - targetElement.element().offsetTop);
    }
    private void dragEventHandler(MouseEvent evt) {
        if (targetElement!=null) {
            evt.preventDefault();
            evt.stopPropagation();
            move(evt);
        }
    }
    private void dropEventHandler(MouseEvent evt) {
        if (targetElement!=null) {
            evt.preventDefault();
            evt.stopPropagation();
            hide();
            int newX = (int) (evt.clientX - dragOffsetX);
            int newY = (int) (evt.clientY - dragOffsetY);
            int deltaX = targetElement.element().offsetLeft - newX;
            int deltaY = targetElement.element().offsetTop - newY;
            // actionManager.move(targetElement, deltaX, deltaY);
            targetElement = null;
        }
    }
    private void visible() {
        container.element().style.display = "block";
        container.element().style.left = targetElement.element().offsetLeft + "px";
        container.element().style.top = targetElement.element().offsetTop + "px";
        container.element().style.width = CSSProperties.WidthUnionType.of(targetElement.element().offsetWidth + "px");
        container.element().style.height = CSSProperties.HeightUnionType.of(targetElement.element().offsetHeight + "px");
    }
    private void move(MouseEvent evt) {
        int newX = (int) (evt.clientX - dragOffsetX);
        int newY = (int) (evt.clientY - dragOffsetY);
        container.element().style.left = newX + "px";
        container.element().style.top = newY + "px";
    }
    private void hide() {
        container.element().style.display = "none";
    }
}
