package dev.sayaya.handbook.client.interfaces.canvas;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.BoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.BoxElementList;
import dev.sayaya.handbook.client.usecase.CanvasMode;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.elemento.Elements.div;

@Singleton
public class CanvasElement extends HTMLContainerBuilder<HTMLDivElement> {
    @Inject CanvasElement(BoxElementList elements, CanvasMode mode, ActionManager actionManager, CanvasContextMenuElement contextElement) {
        this(div(), elements, mode, actionManager, contextElement);
    }
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private final CanvasMode mode;
    private final ActionManager actionManager;
    private final CanvasContextMenuElement contextElement;
    private CanvasElement(HTMLContainerBuilder<HTMLDivElement> container, BoxElementList elements, CanvasMode mode, ActionManager actionManager, CanvasContextMenuElement contextElement) {
        super(container.element());
        this.container = container;
        this.mode = mode;
        this.actionManager = actionManager;
        this.contextElement = contextElement;
        container.css("canvas").attr("tabindex", "0").add(contextElement);
        on(EventType.click, this::handleContext);
        on(EventType.contextmenu, this::handleContext);
        on(EventType.keypress, this::handleKeyPress);
        elements.distinct().subscribe(this::update);
    }
    private final List<BoxElement> children = new LinkedList<>();
    private void update(BoxElement[] elems) {
        // 이전 상태와 새 상태를 Set으로 변환하여 비교
        var newSet = Arrays.stream(elems).map(BoxElement::toDomain).collect(Collectors.toSet());
        var newElementMap = Arrays.stream(elems).collect(Collectors.toMap(BoxElement::toDomain, elem -> elem));
        var prevSet = children.stream().map(BoxElement::toDomain).collect(Collectors.toSet());
        // 1. 이전 상태에서 없어진 요소 제거
        children.removeIf(child -> {
            if (!newSet.contains(child.toDomain())) {
                child.element().remove(); // DOM에서 제거
                return true; // 제거 대상
            } else return false; // 유지
        });
        // 2. 새로 추가할 요소만 추가
        for (Box domain : newSet) if (!prevSet.contains(domain)) {
            BoxElement elem = newElementMap.get(domain);
            container.add(elem);
            children.add(elem);
        }
    }
    private void handleContext(MouseEvent evt) {
        evt.preventDefault();
        contextElement.offset((int) evt.clientX, (int)(evt.clientY));
        contextElement.toggle();
    }
    private void handleKeyPress(KeyboardEvent evt) {
        if(evt.ctrlKey) {
            if(evt.code.equals("KeyZ")) {
                if (evt.shiftKey) actionManager.redo();
                else actionManager.undo();
            }
        }
    }
}

