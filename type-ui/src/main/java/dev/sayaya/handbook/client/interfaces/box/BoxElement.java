package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.handbook.client.domain.value.ValueListElement;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasContextMenuElement;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import dev.sayaya.rx.subject.Subject;
import dev.sayaya.ui.elements.CardElementBuilder;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.*;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.sayaya.rx.subject.Subject.subject;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.CardElementBuilder.card;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class BoxElement extends HTMLContainerBuilder<HTMLDivElement> implements UpdatableBox {
    private final Box box;
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private final CardElementBuilder<?, ?> card;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder title;
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder btnAdd;
    private final Subject<List<Value>> values = (Subject) subject(List.class);
    private final SelectedBoxElement selected;
    private final DragShapeElement dragShapeElement;
    private final BoxContextMenuElement context;
    private final CanvasContextMenuElement canvasContext;
    private double dragStartTimer;
    @AssistedInject BoxElement(@Assisted Box box, ActionManager actionManager, SelectedBoxElement selected, DragShapeElement dragShapeElement, BoxDisplayMode mode,
                               BoxContextMenuElement context, CanvasContextMenuElement canvasContext, ValueListElement.ValueListElementFactory valueListFactory) {
        this(div(), box, actionManager, selected, dragShapeElement, mode, context, canvasContext, valueListFactory);
    }
    private BoxElement(HTMLContainerBuilder<HTMLDivElement> container, Box box, ActionManager actionManager, SelectedBoxElement selected, DragShapeElement dragShapeElement, BoxDisplayMode mode,
                       BoxContextMenuElement context, CanvasContextMenuElement canvasContext, ValueListElement.ValueListElementFactory valueListFactory) {
        super(container.element());
        if (box == null) throw new IllegalArgumentException("Box must not be null.");
        this.box = box;

        this.container = container.css("type-box");
        this.card = card().outlined().css("card").attr("tabindex", "0");
        this.title = textField().outlined().css("label");
        this.btnAdd = button().icon().add(icon("add")).css("add");
        container.add(card.add(title).add(valueListFactory.valueList(this.values))
                .add(btnAdd));
        update();
        mode.subscribe(this::setMode);
        selected.subscribe(selectedBox -> setSelected(selectedBox.contains(this)));

        this.selected = selected;
        this.dragShapeElement = dragShapeElement;
        this.context = context;
        this.canvasContext = canvasContext;
        attachEventHandlers(container, this, actionManager);
    }
    private void setMode(BoxDisplayState mode) {
        if (mode == BoxDisplayState.SIMPLE) {
            container.attr("simple", true);
            container.element().removeAttribute("detail");
        } else if (mode == BoxDisplayState.DETAIL) {
            container.attr("detail", true);
            container.element().removeAttribute("simple");
        }
    }
    private void setSelected(boolean isSelected) {
        if (isSelected) container.element().setAttribute("selected", "");
        else container.element().removeAttribute("selected");
    }
    private void attachEventHandlers(HTMLContainerBuilder<HTMLDivElement> container, BoxElement boxElement, ActionManager actionManager) {
        container.on(EventType.click, evt -> handleClick(evt, boxElement));
        container.on(EventType.focus, evt-> handleFocus(evt, boxElement));
        container.on(EventType.contextmenu, evt -> handleContextMenu(evt, boxElement));
        container.on(EventType.mousedown, evt -> handleMouseDown(evt, boxElement));
        container.on(EventType.mouseup, this::clearDragStartTimer);
        container.on(EventType.mousemove, this::clearDragStartTimer);
        container.on(EventType.keydown, evt->handleKeyPress(evt, actionManager));
        title.on(EventType.change, evt -> {
            var ipt = Js.asPropertyMap(evt.target);
            actionManager.title(boxElement, ipt.get("value").toString());
        });
        btnAdd.on(EventType.click, evt -> {
            actionManager.addValue(boxElement);
        });
    }
    private void handleClick(MouseEvent evt, BoxElement boxElement) {
        var element = (HTMLElement) evt.target;
        evt.stopPropagation();
        handleSelect(boxElement, evt.ctrlKey);
        context.close();
        if(element != null && element!= DomGlobal.document.activeElement && !element.contains(DomGlobal.document.activeElement)) {
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
    @Override
    public Box box() {
        return box;
    }
    @Override
    public void update() {
        title.value(box.name());
        container.element().style.left = box.x() + "px";
        container.element().style.top = box.y() + "px";
        card.element().style.width = CSSProperties.WidthUnionType.of(box.width() + "px");
        card.element().style.height = CSSProperties.HeightUnionType.of(box.height() + "px");
        values.next(box.values());
    }
}
