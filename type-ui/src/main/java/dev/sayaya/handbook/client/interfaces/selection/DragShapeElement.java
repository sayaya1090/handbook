package dev.sayaya.handbook.client.interfaces.selection;

import elemental2.dom.*;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

import static org.jboss.elemento.Elements.div;

// BoxElement 의 Drag&Drop 기능을 부여함
// Box 클릭 시 드래그 이벤트를 발생하여 실행하고 활성화, release 시 비활성화
@Singleton
public class DragShapeElement extends HTMLContainerBuilder<HTMLDivElement> {
    @Inject DragShapeElement(SelectedBoxElement selected) {
        this(div(), selected);
    }
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private final Set<DropEventHandler> handlers = new HashSet<>();
    private final SelectedBoxElement selected;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private DragShapeElement(HTMLContainerBuilder<HTMLDivElement> container, SelectedBoxElement selected) {
        super(container.element());
        this.container = container;
        this.selected = selected;
        container.attr("draggable", true).css("drag-shape");
        on(EventType.dragstart, this::dragStartEventHandler);
        on(EventType.drag, this::dragEventHandler);
        on(EventType.dragend, this::dropEventHandler);
        on(EventType.mouseup, evt->hide());
        hide();
    }
    public void triggerDragEvent() {
        var param = DragEventInit.create();
        param.setBubbles(true);
        param.setCancelable(true);
        var dragEvent = new DragEvent("dragstart", param);
        element().dispatchEvent(dragEvent);
    }
    private void dragStartEventHandler(MouseEvent evt) {
        var target = selected.getValue().stream().findFirst().orElse(null);
        if(target==null) return;
        visible(target.element());
        dragOffsetX = (int) (evt.clientX - target.element().offsetLeft);
        dragOffsetY = (int) (evt.clientY - target.element().offsetTop);
    }
    private void dragEventHandler(MouseEvent evt) {
        DomGlobal.console.log("dragEventHandler");
        evt.preventDefault();
        evt.stopPropagation();
        move(evt);
    }
    private void dropEventHandler(MouseEvent evt) {
        evt.preventDefault();
        evt.stopPropagation();
        hide();
        var target = selected.getValue().stream().findFirst().orElse(null);
        if(target==null) return;
        int newX = (int) (evt.clientX - dragOffsetX);
        int newY = (int) (evt.clientY - dragOffsetY);
        int deltaX = newX - target.element().offsetLeft;
        int deltaY = newY - target.element().offsetTop;
        for(var handler: handlers) handler.onInvoke(deltaX, deltaY, target);
    }
    private void visible(HTMLElement element) {
        container.element().style.left = (element.offsetLeft-2) + "px";
        container.element().style.top = (element.offsetTop-2) + "px";
        container.element().style.width = CSSProperties.WidthUnionType.of(element.offsetWidth + "px");
        container.element().style.height = CSSProperties.HeightUnionType.of(element.offsetHeight + "px");
        container.element().style.display = "block";
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
    public void onDrop(DropEventHandler handler) {
        handlers.add(handler);
    }
}
