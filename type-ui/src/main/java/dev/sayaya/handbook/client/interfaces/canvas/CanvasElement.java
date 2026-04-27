package dev.sayaya.handbook.client.interfaces.canvas;

import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.interfaces.box.BoxContextMenuElement;
import dev.sayaya.handbook.client.interfaces.box.BoxElementFactory;
import dev.sayaya.handbook.client.interfaces.box.BoxReferenceElement;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.handbook.client.interfaces.box.VersionHistoryPanel;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.CanvasMode;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.client.usecase.action.MoveBoxAction;
import dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction;
import elemental2.dom.*;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static org.jboss.elemento.Elements.div;

/**
 * 타입 스키마 편집기의 메인 캔버스 컴포넌트.
 *
 * <p><b>책임:</b> 타입 박스({@link TypeElement})들을 배치/렌더링하고,
 * 드래그 이동, 키보드 단축키(Ctrl+Z Undo, Ctrl+A 전체 선택, Delete 삭제, Arrow 이동),
 * 컨텍스트 메뉴, 선택 상태를 관리한다.
 * TypeList를 구독하여 타입 추가/제거 시 DOM 요소를 동기화한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link BoxElementFactory} — TypeElement 생성 팩토리</li>
 *   <li>{@link TypeList} — 타입 목록 상태 구독</li>
 *   <li>{@link PositionMap} — 타입 위치 조회</li>
 *   <li>{@link ActionManager} — Move/Delete/ComplexAction 실행</li>
 *   <li>{@link SelectedBoxElement} — 선택 상태</li>
 *   <li>{@link DragShapeElement} — 드래그 시각 피드백</li>
 *   <li>{@link CanvasMode}, {@link GridSnap} — 모드/스냅 설정</li>
 *   <li>{@link CanvasContextMenuElement}, {@link BoxContextMenuElement} — 컨텍스트 메뉴</li>
 * </ul></p>
 * <p><b>주의:</b> elementMap으로 typeKey → TypeElement 매핑을 유지한다.
 * 드래그 종료 시 스냅이 활성화되면 첫 번째 선택 박스 기준으로 스냅 델타를 계산한다.
 * 모바일에서는 {@link TouchEventAdapter}가 터치 이벤트를 마우스 이벤트로 변환하고,
 * {@link PinchZoomHandler}가 두 손가락 핀치 줌을 처리한다.</p>
 */
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
    private final TouchEventAdapter touchAdapter;
    private final PinchZoomHandler pinchZoom;
    private final Map<String, TypeElement> elementMap = new LinkedHashMap<>();

    @Inject
    CanvasElement(BoxElementFactory boxFactory, TypeList typeList, ActionManager actionManager,
                  PositionMap positionMap, SelectedBoxElement selection, DragShapeElement dragShape,
                  BoxReferenceElement referenceElement, ChangeTracker tracker,
                  CanvasMode canvasMode, GridSnap gridSnap,
                  CanvasContextMenuElement canvasMenu, BoxContextMenuElement boxMenu,
                  TouchEventAdapter touchAdapter, PinchZoomHandler pinchZoom,
                  VersionHistoryPanel versionHistoryPanel) {
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
        this.touchAdapter = touchAdapter;
        this.pinchZoom = pinchZoom;

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
                .add(versionHistoryPanel)
                .element();

        // 모바일 터치 지원: 터치 → 마우스 이벤트 변환, 핀치 줌
        touchAdapter.bind(root);
        pinchZoom.bind(root);

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
        touchAdapter.bind(elem.element());
        elem.element().addEventListener("mousedown", e -> {
            MouseEvent me = (MouseEvent) e;
            if (me.button != 0) return;
            e.preventDefault(); // 텍스트 셀렉션 방지
            canvasMode.getCurrentState().onTypeMouseDown(me, dragShape);
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
        canvasMode.getCurrentState().onCanvasKeyDown(e, () -> {
            if (e.ctrlKey && "z".equals(e.key)) {
                e.preventDefault();
                if (e.shiftKey) actionManager.redo();
                else actionManager.undo();
            } else if (e.ctrlKey && ("a".equals(e.key) || "A".equals(e.key))) {
                e.preventDefault();
                Set<String> allKeys = new HashSet<>();
                for (TypeValue type : typeList.getValue()) {
                    allKeys.add(type.key());
                }
                selection.selectAll(allKeys);
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
        });
    }

    public TypeElement getElement(String typeKey) {
        return elementMap.get(typeKey);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
