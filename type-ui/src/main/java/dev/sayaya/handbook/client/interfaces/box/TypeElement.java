package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasContextMenuElement;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.interfaces.value.ValueListElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.UpdatableType;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.CardElementBuilder;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import elemental2.dom.*;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.HashSet;
import java.util.Set;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.CardElementBuilder.card;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static org.jboss.elemento.Elements.div;

public class TypeElement extends HTMLContainerBuilder<HTMLDivElement> implements UpdatableType {
    private Type type;
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private final CardElementBuilder<?, ?> card;
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder btnAdd;
    private final SelectedBoxElement selected;
    private final DragShapeElement dragShapeElement;
    private final BoxContextMenuElement context;
    private final CanvasContextMenuElement canvasContext;
    @Delegate private final BehaviorSubject<TypeElement> subject = behavior(this);
    private double dragStartTimer;
    @AssistedInject TypeElement(@Assisted Type type, ActionManager actionManager, SelectedBoxElement selected, DragShapeElement dragShapeElement, BoxDisplayMode mode,
                TypeNameElement.TypeNameElementFactory typeNameFactory, TypeVersionElement.TypeVersionElementFactory typeVersionFactory,
                TypeEffectDateElement.TypeEffectDateElementFactory typeEffectDateFactory, TypeExpireDateElement.TypeExpireDateElementFactory typeExpireDateFactory,
                BoxContextMenuElement context, CanvasContextMenuElement canvasContext, ValueListElement.ValueListElementFactory valueListFactory) {
        this(div(), type, actionManager, selected, dragShapeElement, mode, typeNameFactory, typeVersionFactory, typeEffectDateFactory, typeExpireDateFactory, context, canvasContext, valueListFactory);
    }
    private TypeElement(HTMLContainerBuilder<HTMLDivElement> container, Type type, ActionManager actionManager, SelectedBoxElement selected, DragShapeElement dragShapeElement, BoxDisplayMode mode,
                        TypeNameElement.TypeNameElementFactory typeNameFactory,
                        TypeVersionElement.TypeVersionElementFactory typeVersionFactory,
                        TypeEffectDateElement.TypeEffectDateElementFactory typeEffectDateFactory,
                        TypeExpireDateElement.TypeExpireDateElementFactory typeExpireDateFactory,
                        BoxContextMenuElement context, CanvasContextMenuElement canvasContext, ValueListElement.ValueListElementFactory valueListFactory) {
        super(container.element());
        if (type == null) throw new IllegalArgumentException("Box must not be null.");
        this.type = type;
        this.container = container.css("type-box");
        this.card = card().outlined().css("card").attr("tabindex", "0");
        var title = typeNameFactory.create(this);
        var version = typeVersionFactory.create(this);
        var effectDate = typeEffectDateFactory.create(this);
        var expireDate = typeExpireDateFactory.create(this);
        this.btnAdd = button().icon().add(icon("add")).css("add");
        container.add(card.add(title)
                .add(div().style("""
                        display: grid;
                        justify-items: stretch;
                        grid-auto-flow: column;
                        grid-template-rows: 1fr 1fr;
                        gap: 0.5rem;
                        padding: 0.5rem;
                        """).add(effectDate).add(expireDate).add(version))
                .add(valueListFactory.valueList(subject))
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
    private void attachEventHandlers(HTMLContainerBuilder<HTMLDivElement> container, TypeElement typeBoxElement, ActionManager actionManager) {
        container.on(EventType.click, evt -> handleClick(evt, typeBoxElement));
        container.on(EventType.focus, evt-> handleFocus(evt, typeBoxElement));
        container.on(EventType.contextmenu, evt -> handleContextMenu(evt, typeBoxElement));
        container.on(EventType.mousedown, evt -> handleMouseDown(evt, typeBoxElement));
        container.on(EventType.mouseup, this::clearDragStartTimer);
        container.on(EventType.mousemove, this::clearDragStartTimer);
        container.on(EventType.touchstart, evt -> handleTouch(evt, typeBoxElement));
        container.on(EventType.touchmove, this::clearDragStartTimer);
        container.on(EventType.touchend, this::clearDragStartTimer);
        container.on(EventType.keydown, evt->handleKeyPress(evt, actionManager));
        btnAdd.on(EventType.click, evt -> {
            var attr = Attribute.builder()
                    .id(type.id() + "$$$" + type.version() + "$$$" + type.attributes().size())
                    .name("New Attribute")
                    .type(AttributeTypeDefinition.builder()
                            .baseType(AttributeTypeDefinition.AttributeType.Value)
                            .build()
                    ).build();
            var next = type.toBuilder().attribute(attr).height(type.height() + 42).build();
            actionManager.edit(this, next);
        });
    }
    private void handleClick(MouseEvent evt, TypeElement typeBoxElement) {
        var element = (HTMLElement) evt.target;
        evt.stopPropagation();
        handleSelect(typeBoxElement, evt.ctrlKey);
        context.close();
        if(element != null && element!= DomGlobal.document.activeElement && !element.contains(DomGlobal.document.activeElement)) {
            DomGlobal.document.activeElement.blur();
            element.focus();
        }
    }
    private void handleFocus(FocusEvent evt, TypeElement typeBoxElement) {
        if(selected.getValue().contains(typeBoxElement)) return;
        else handleSelect(typeBoxElement, false);
    }
    private void handleSelect(TypeElement typeBoxElement, boolean shouldMultiple) {
        if(shouldMultiple) {
            var nextSelectedBoxes = new HashSet<>(selected.getValue());
            if(nextSelectedBoxes.contains(typeBoxElement)) nextSelectedBoxes.remove(typeBoxElement);
            else nextSelectedBoxes.add(typeBoxElement);
            selected.next(nextSelectedBoxes);
        } else selected.next(Set.of(typeBoxElement));
    }
    private void handleContextMenu(MouseEvent evt, TypeElement typeBoxElement) {
        evt.preventDefault();
        evt.stopPropagation();
        handleSelect(typeBoxElement, evt.ctrlKey);
        context.offset((int) evt.clientX, (int)(evt.clientY));
        context.toggle();
        canvasContext.close();
    }
    private void handleMouseDown(MouseEvent evt, TypeElement typeBoxElement) {
        dragStartTimer = DomGlobal.setTimeout(v -> {
            handleSelect(typeBoxElement, evt.ctrlKey);
            dragShapeElement.triggerDragEvent();
        }, 150);
    }
    private void handleTouch(TouchEvent evt, TypeElement typeBoxElement) {
        dragStartTimer = DomGlobal.setTimeout(v -> {
            handleSelect(typeBoxElement, evt.ctrlKey);
            dragShapeElement.triggerDragEvent();
        }, 150);
    }
    private void clearDragStartTimer(MouseEvent evt) {
        if (dragStartTimer != 0) DomGlobal.clearTimeout(dragStartTimer);
    }
    private void clearDragStartTimer(TouchEvent evt) {
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
        actionManager.move(dx, dy, selected.getValue().stream().toArray(TypeElement[]::new));
    }
    public void update(Type type) {
        if (type == null) throw new IllegalArgumentException("Type must not be null.");
        this.type = type;
        update();
    }
    @Override
    public Type value() {
        return type;
    }
    @Override
    public void update() {
        if(type == null) throw new IllegalStateException("Type is not set.");
        if(type.state() == Type.TypeState.DELETE) {
            container.element().remove();
            return;
        }
        container.element().style.left = type.x() + "px";
        container.element().style.top = type.y() + "px";
        card.element().style.width = CSSProperties.WidthUnionType.of(type.width() + "px");
        card.element().style.height = CSSProperties.HeightUnionType.of(type.height() + "px");
        subject.next(this);
    }
}
