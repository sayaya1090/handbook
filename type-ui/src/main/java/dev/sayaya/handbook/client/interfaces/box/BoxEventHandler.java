package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasContextMenuElement;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import elemental2.dom.*;
import jsinterop.base.Js;
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
        container.on(EventType.focus, evt-> handleFocus(evt, boxElement));
        container.on(EventType.contextmenu, evt -> handleContextMenu(evt, boxElement));
        container.on(EventType.mousedown, evt -> handleMouseDown(evt, boxElement));
        container.on(EventType.mouseup, this::clearDragStartTimer);
        container.on(EventType.mousemove, this::clearDragStartTimer);
        container.on(EventType.keydown, evt->handleKeyPress(evt, actionManager));
    }
    private void handleClick(MouseEvent evt, BoxElement boxElement) {
        var element = (HTMLElement) evt.target;
        evt.stopPropagation();
        handleSelect(boxElement, evt.ctrlKey);
        context.close();
        if(element != null && element!=DomGlobal.document.activeElement && !element.contains(DomGlobal.document.activeElement)) {
            DomGlobal.document.activeElement.blur();
            element.focus();
        }
    }
    private void handleFocus(FocusEvent evt, BoxElement boxElement) {
        if(selected.getValue().contains(boxElement)) return;
        else handleSelect(boxElement, false);
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
        evt.preventDefault();
        evt.stopPropagation();
        handleSelect(boxElement, evt.ctrlKey);
        context.offset((int) evt.clientX, (int)(evt.clientY));
        context.toggle();
        canvasContext.close();
    }
    private void handleMouseDown(MouseEvent evt, BoxElement boxElement) {
        dragStartTimer = DomGlobal.setTimeout(v -> {
            handleSelect(boxElement, evt.ctrlKey);
            dragShapeElement.triggerDragEvent();
        }, 200);
    }
    private void clearDragStartTimer(MouseEvent evt) {
        if (dragStartTimer != 0) DomGlobal.clearTimeout(dragStartTimer);
    }
    private void handleKeyPress(KeyboardEvent evt, ActionManager actionManager) {
        var element = (HTMLElement) evt.target;
        if(!element.classList.contains("card")) return;
        int dx = 0;
        int dy = 0;
        if("ArrowUp".equalsIgnoreCase(evt.key)) dy = -1;
        if("ArrowDown".equalsIgnoreCase(evt.key)) dy = 1;
        if("ArrowLeft".equalsIgnoreCase(evt.key)) dx = -1;
        if("ArrowRight".equalsIgnoreCase(evt.key)) dx = 1;
        actionManager.move(dx, dy, selected.getValue().stream().toArray(BoxElement[]::new));
    }
    public void attachUiEventHandlers(BoxElement boxElement, ActionManager actionManager, BoxUi ui, Box box) {
        ui.attachTitleHandler(EventType.change, evt -> {
            var ipt = Js.asPropertyMap(evt.target);
            actionManager.title(boxElement, ipt.get("value").toString());
        });
        ui.attachAddButtonHandler(EventType.click, evt -> {
            actionManager.addValue(boxElement);
        });
    }

}
