package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasContextMenuElement;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BoxElementFactory {
    private final Map<Box, BoxElement> cache = new ConcurrentHashMap<>();
    private final ActionManager actionManager;
    private final SelectedBoxElement selected;
    private final DragShapeElement dragShapeElement;
    private final BoxDisplayMode mode;
    private final BoxContextMenuElement context;
    private final CanvasContextMenuElement canvasContext;
    @Inject BoxElementFactory(ActionManager actionManager, SelectedBoxElement selected,
                              DragShapeElement dragShapeElement,
                              BoxDisplayMode mode, BoxContextMenuElement context, CanvasContextMenuElement canvasContext) {
        this.actionManager = actionManager;
        this.selected = selected;
        this.dragShapeElement = dragShapeElement;
        this.mode = mode;
        this.context = context;
        this.canvasContext = canvasContext;
    }
    public BoxElement create(Box box) {
        return cache.computeIfAbsent(box, b -> new BoxElement(box, actionManager, selected, dragShapeElement, mode, context, canvasContext));
    }
}
