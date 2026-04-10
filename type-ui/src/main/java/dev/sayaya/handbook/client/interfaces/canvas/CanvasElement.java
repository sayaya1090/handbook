package dev.sayaya.handbook.client.interfaces.canvas;

import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.interfaces.box.BoxContextMenuElement;
import dev.sayaya.handbook.client.interfaces.box.BoxElementFactory;
import dev.sayaya.handbook.client.interfaces.box.BoxReferenceElement;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.CanvasMode;
import dev.sayaya.handbook.client.usecase.ChangeTracker;
import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.client.usecase.action.MoveBoxAction;
import dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static org.jboss.elemento.Elements.div;

@Singleton
public class CanvasElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final BoxElementFactory boxFactory;
    private final ActionManager actionManager;
    private final PositionMap positionMap;
    private final SelectedBoxElement selection;
    private final DragShapeElement dragShape;
    private final TypeList typeList;
    private final ChangeTracker tracker;
    private final CanvasContextMenuElement canvasMenu;
    private final BoxContextMenuElement boxMenu;
    private final CanvasMode canvasMode;
    private final GridSnap gridSnap;
    private final Map<String, TypeElement> elementMap = new LinkedHashMap<>();

    @Inject
    CanvasElement(BoxElementFactory boxFactory, TypeList typeList, ActionManager actionManager,
                  PositionMap positionMap, SelectedBoxElement selection, DragShapeElement dragShape,
                  BoxReferenceElement referenceElement, ChangeTracker tracker,
                  CanvasMode canvasMode, GridSnap gridSnap,
                  CanvasContextMenuElement canvasMenu, BoxContextMenuElement boxMenu) {
        this.canvasMode = canvasMode;
        this.gridSnap = gridSnap;
        this.boxFactory = boxFactory;
        this.actionManager = actionManager;
        this.positionMap = positionMap;
        this.selection = selection;
        this.dragShape = dragShape;
        this.typeList = typeList;
        this.tracker = tracker;
        this.canvasMenu = canvasMenu;
        this.boxMenu = boxMenu;

        dragShape.onDrop(delta -> {
            Set<String> selected = new HashSet<>(selection.getValue());
            if (!selected.isEmpty() && (delta[0] != 0 || delta[1] != 0)) {
                // 스냅: 첫 번째 선택 박스 기준으로 스냅 델타 계산
                int dx = delta[0], dy = delta[1];
                if (gridSnap.isEnabled()) {
                    String firstKey = selected.iterator().next();
                    Position firstPos = positionMap.get(firstKey);
                    if (firstPos != null) {
                        dx = gridSnap.snapDelta(firstPos.x, dx);
                        dy = gridSnap.snapDelta(firstPos.y, dy);
                    }
                }
                if (dx == 0 && dy == 0) return;
                MoveBoxAction move = new MoveBoxAction(positionMap, selected, dx, dy);
                Action[] pushOuts = selected.stream()
                        .map(key -> new PushOutOverlapAction(positionMap, key, 10))
                        .toArray(Action[]::new);
                Action[] all = new Action[1 + pushOuts.length];
                all[0] = move;
                System.arraycopy(pushOuts, 0, all, 1, pushOuts.length);
                actionManager.execute(new ComplexAction(all));
            }
        });

        root = div().css("type-canvas")
                .attr("tabindex", "0")
                .add(referenceElement)
                .add(dragShape)
                .add(canvasMenu)
                .add(boxMenu)
                .element();

        root.addEventListener("click", e -> selection.clear());
        root.addEventListener("contextmenu", e -> {
            e.preventDefault();
            MouseEvent me = (MouseEvent) e;
            boxMenu.hide();
            canvasMenu.show((int) me.clientX, (int) me.clientY);
        });
        root.addEventListener("keydown", e -> handleKeyDown((KeyboardEvent) e));
        root.addEventListener("mousemove", e -> handleMouseMove((MouseEvent) e));
        root.addEventListener("mouseup", e -> handleMouseUp((MouseEvent) e));

        typeList.subscribe(this::syncElements);
    }

    private void syncElements(Set<TypeValue> types) {
        Set<String> currentKeys = new HashSet<>(elementMap.keySet());
        Set<String> newKeys = new HashSet<>();
        for (TypeValue type : types) {
            String key = type.key();
            newKeys.add(key);
            if (!elementMap.containsKey(key)) {
                Position pos = positionMap.get(key);
                if (pos == null) pos = Position.of(20, 20, 240, 160);
                TypeElement elem = boxFactory.create(type, pos);
                elementMap.put(key, elem);
                root.appendChild(elem.element());
                initBoxHandlers(elem);
            } else {
                elementMap.get(key).setType(type);
            }
        }
        for (String key : currentKeys) {
            if (!newKeys.contains(key)) {
                TypeElement removed = elementMap.remove(key);
                if (removed != null) root.removeChild(removed.element());
            }
        }
    }

    private void initBoxHandlers(TypeElement elem) {
        elem.element().addEventListener("mousedown", e -> {
            MouseEvent me = (MouseEvent) e;
            if (me.button != 0) return;
            e.preventDefault(); // 텍스트 셀렉션 방지
            if (canvasMode.isLayoutMode()) dragShape.show((int) me.clientX, (int) me.clientY);
        });
        elem.element().addEventListener("contextmenu", e -> {
            e.preventDefault();
            e.stopPropagation();
            MouseEvent me = (MouseEvent) e;
            canvasMenu.hide();
            boxMenu.show((int) me.clientX, (int) me.clientY, elem.getType().key());
        });
    }

    private void handleMouseMove(MouseEvent e) {
        if (!dragShape.isActive()) return;
        dragShape.move((int) e.clientX, (int) e.clientY);
    }

    private void handleMouseUp(MouseEvent e) {
        if (!dragShape.isActive()) return;
        dragShape.drop((int) e.clientX, (int) e.clientY);
    }

    private void handleKeyDown(KeyboardEvent e) {
        if (!canvasMode.isEditable()) return;
        if (e.ctrlKey && "z".equals(e.key)) {
            e.preventDefault();
            if (e.shiftKey) actionManager.redo();
            else actionManager.undo();
        } else if ("Delete".equals(e.key) || "Backspace".equals(e.key)) {
            e.preventDefault();
            Set<String> selected = new HashSet<>(selection.getValue());
            for (TypeValue type : typeList.getValue()) {
                if (selected.contains(type.key())) {
                    actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
                }
            }
            selection.clear();
        } else if (e.key != null && e.key.startsWith("Arrow")) {
            e.preventDefault();
            Set<String> selected = selection.getValue();
            if (selected.isEmpty()) return;
            int step = gridSnap.isEnabled() ? 20 : (e.shiftKey ? 20 : 5);
            int dx = 0, dy = 0;
            switch (e.key) {
                case "ArrowUp":    dy = -step; break;
                case "ArrowDown":  dy = step;  break;
                case "ArrowLeft":  dx = -step; break;
                case "ArrowRight": dx = step;  break;
            }
            if (dx != 0 || dy != 0) {
                Set<String> keys = new HashSet<>(selected);
                MoveBoxAction move = new MoveBoxAction(positionMap, keys, dx, dy);
                Action[] pushOuts = keys.stream()
                        .map(key -> new PushOutOverlapAction(positionMap, key, 10))
                        .toArray(Action[]::new);
                Action[] all = new Action[1 + pushOuts.length];
                all[0] = move;
                System.arraycopy(pushOuts, 0, all, 1, pushOuts.length);
                actionManager.execute(new ComplexAction(all));
            }
        }
    }

    public TypeElement getElement(String typeKey) {
        return elementMap.get(typeKey);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
