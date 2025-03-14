package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasContextMenuElement;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;

public class BoxElement extends HTMLContainerBuilder<HTMLDivElement> implements UpdatableBox {
    private final Box box;
    private final BoxUi ui;
    BoxElement(Box box, ActionManager actionManager, SelectedBoxElement selected, DragShapeElement dragShapeElement,
               BoxDisplayMode mode,
               BoxContextMenuElement context, CanvasContextMenuElement canvasContext) {
        this(new BoxUi(), box, actionManager, selected, dragShapeElement, mode, context, canvasContext);
    }
    private BoxElement(BoxUi ui, Box box, ActionManager actionManager, SelectedBoxElement selected, DragShapeElement dragShapeElement,
                       BoxDisplayMode mode,
                       BoxContextMenuElement context, CanvasContextMenuElement canvasContext) {
        super(ui.getContainerElement().element());
        if (box == null) throw new IllegalArgumentException("Box must not be null.");
        this.box = box;
        this.ui = ui;
        ui.update(box);
        mode.subscribe(ui::setMode);
        selected.subscribe(selectedBox -> ui.setSelected(selectedBox.contains(this)));

        BoxEventHandler eventHandler = new BoxEventHandler(selected, dragShapeElement, context, canvasContext);
        eventHandler.attachEventHandlers(ui.getContainerElement(), this, actionManager);
        eventHandler.attachUiEventHandlers(this, actionManager, ui, box);
    }

    @Override
    public Box box() {
        return box;
    }
    @Override
    public void update() {
        ui.update(box);
    }
}
