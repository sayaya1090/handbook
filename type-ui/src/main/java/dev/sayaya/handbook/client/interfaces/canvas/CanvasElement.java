package dev.sayaya.handbook.client.interfaces.canvas;

import dev.sayaya.handbook.client.interfaces.box.BoxContextMenuElement;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.handbook.client.interfaces.box.TypeElementList;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.interfaces.value.AttributeEditorDialog;
import dev.sayaya.handbook.client.usecase.ActionManager;
import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.elemento.Elements.div;

@Singleton
public class CanvasElement implements IsElement<HTMLDivElement> {
    private static final String KEY_Z = "KeyZ";
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div();
    private final CanvasMode mode;
    private final ActionManager actionManager;
    private final CanvasContextMenuElement contextElement;
    private final BoxContextMenuElement boxContextMenuElement;
    private final List<TypeElement> children = new LinkedList<>();

    @Inject CanvasElement(TypeElementList elements, CanvasMode mode, ActionManager actionManager,
                          CanvasContextMenuElement contextElement,
                          BoxContextMenuElement boxContextMenuElement,
                          AttributeEditorDialog attributeEditor,
                          SelectedBoxElement selected, DragShapeElement dragElement) {
        this.mode = mode;
        this.actionManager = actionManager;
        this.contextElement = contextElement;
        this.boxContextMenuElement = boxContextMenuElement;
        container.css("canvas").attr("tabindex", "0")
                .add(contextElement)
                .add(dragElement)
                .add(boxContextMenuElement)
                .add(attributeEditor);
        init(container, elements, dragElement, selected);
    }
    private void init(HTMLContainerBuilder<HTMLDivElement> container,
                      TypeElementList elements,
                      DragShapeElement dragElement,
                      SelectedBoxElement selected) {
        initEventHandlers(selected);
        dragElement.onDrop(actionManager::move);
        elements.distinct().subscribe(this::update);
    }
    private void initEventHandlers(SelectedBoxElement selected) {
        on(EventType.contextmenu, this::handleContext);
        on(EventType.keydown, this::handleKeyPress);
        on(EventType.dragover, Event::preventDefault);  // Drag 시 X 출력 제거
        on(EventType.click, evt->{
            if(evt.currentTarget == element()) selected.next(Set.of());
            contextElement.close();
            boxContextMenuElement.close();
        });
        contextElement.on(EventType.click, evt->element().focus());
        boxContextMenuElement.on(EventType.click, evt->element().focus());
    }

    private void update(TypeElement[] elems) {
        var newElementMap = Arrays.stream(elems).collect(Collectors.toMap(e->e.value(), elem -> elem));
        // 1. 이전 상태에서 없어진 요소 제거
        children.removeIf(child -> {
            if (!newElementMap.containsKey(child.value())) {
                child.element().remove();
                return true;
            } else return false; // 유지
        });
        // 2. 새로 추가할 요소만 추가
        newElementMap.forEach((domain, elem) -> {
            if (children.stream().noneMatch(child -> child.value().equals(domain))) {
                container.add(elem);
                children.add(elem);
            }
        });
    }
    private void handleContext(MouseEvent evt) {
        evt.preventDefault();
        contextElement.offset((int) evt.clientX, (int)(evt.clientY));
        contextElement.toggle();
        boxContextMenuElement.close();
    }
    private void handleKeyPress(KeyboardEvent evt) {
        if(evt.ctrlKey) {
            if(evt.code.equals(KEY_Z)) {
                if (evt.shiftKey) actionManager.redo();
                else actionManager.undo();
                element().focus();
            }
        }
    }
}

