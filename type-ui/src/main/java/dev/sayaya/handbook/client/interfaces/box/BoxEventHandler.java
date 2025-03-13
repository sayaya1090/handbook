package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasContextMenuElement;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.HashSet;
import java.util.Set;

public class BoxEventHandler {
    private final SelectedBoxElement selected;
    private final DragShapeElement dragShapeElement;
    private final BoxContextMenuElement context;
    private final CanvasContextMenuElement canvasContext;
    private double dragStartTimer = 0;

    public BoxEventHandler(SelectedBoxElement selected, DragShapeElement dragShapeElement,
                           BoxContextMenuElement context, CanvasContextMenuElement canvasContext) {
        this.selected = selected;
        this.dragShapeElement = dragShapeElement;
        this.context = context;
        this.canvasContext = canvasContext;
    }

    public void attachEventHandlers(HTMLContainerBuilder<HTMLDivElement> container, BoxElement boxElement, ActionManager actionManager) {
        container.on(EventType.click, evt -> handleClick(evt, boxElement));
        container.on(EventType.contextmenu, evt -> handleContextMenu(evt, boxElement));
        container.on(EventType.mousedown, evt -> handleMouseDown(boxElement));
        container.on(EventType.mouseup, this::clearDragStartTimer);
        container.on(EventType.mousemove, this::clearDragStartTimer);
        container.on(EventType.keydown, evt->handleKeyPress(evt, actionManager, boxElement));
    }
    private void handleClick(MouseEvent evt, BoxElement boxElement) {
        evt.stopPropagation();
        DomGlobal.console.log("Box Clicked for Box: " + boxElement.box().name());
        handleSelect(boxElement, evt.ctrlKey);
        context.close();
    }
    private void handleSelect(BoxElement boxElement, boolean shouldMultiple) {
        if(shouldMultiple) {
            var nextSelectedBoxes = new HashSet<>(selected.getValue());
            if(nextSelectedBoxes.contains(boxElement)) nextSelectedBoxes.remove(boxElement);
            else nextSelectedBoxes.add(boxElement);
            selected.next(nextSelectedBoxes);
        } else selected.next(Set.of(boxElement));
    }

    private void handleContextMenu(MouseEvent evt, BoxElement boxElement) {
        evt.stopPropagation();
        handleSelect(boxElement, evt.ctrlKey);
        context.offset((int) evt.clientX, (int)(evt.clientY));
        context.toggle();
        canvasContext.close();
    }
    private void handleMouseDown(BoxElement boxElement) {
        dragStartTimer = DomGlobal.setTimeout(v -> {
            handleSelect(boxElement, false);
            dragShapeElement.triggerDragEvent();
        }, 200);
    }
    private void clearDragStartTimer(MouseEvent evt) {
        if (dragStartTimer != 0) DomGlobal.clearTimeout(dragStartTimer);
    }
    private void handleKeyPress(KeyboardEvent evt, ActionManager actionManager, BoxElement boxElement) {
        int dx = 0;
        int dy = 0;
        if("ArrowUp".equalsIgnoreCase(evt.key)) dy = -1;
        if("ArrowDown".equalsIgnoreCase(evt.key)) dy = 1;
        if("ArrowLeft".equalsIgnoreCase(evt.key)) dx = -1;
        if("ArrowRight".equalsIgnoreCase(evt.key)) dx = 1;
        actionManager.move(dx, dy, selected.getValue().stream().toArray(BoxElement[]::new));
    }
    public void attachUiEventHandlers(ActionManager actionManager, BoxUi ui, Box box) {
        ui.attachAddButtonHandler(EventType.click, evt -> {
            DomGlobal.console.log("Add Button Clicked for Box: " + box.name());
        });
        ui.attachTitleHandler(EventType.click, evt -> {
            DomGlobal.console.log("Title Clicked for Box: " + box.name());
        });
    }

}
