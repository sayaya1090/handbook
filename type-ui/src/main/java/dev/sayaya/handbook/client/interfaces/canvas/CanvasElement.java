package dev.sayaya.handbook.client.interfaces.canvas;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.box.*;
import dev.sayaya.handbook.client.interfaces.selection.DragShapeElement;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.client.usecase.action.MoveBoxAction;
import dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.domain.TypeLayout;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.elemento.Elements.div;

/**
 * 타입 스키마 편집기의 메인 캔버스 컴포넌트.
 *
 * <p><b>책임:</b> 타입 박스({@link TypeElement})들을 배치/렌더링하고,
 * 드래그 이동, 키보드 단축키(Ctrl+Z Undo, Ctrl+A 전체 선택, Delete 삭제, Arrow 이동),
 * 컨텍스트 메뉴, 선택 상태를 관리한다.
 * TypeSearchProvider를 구독하여 현재 레이아웃 기간에 유효한 타입만 DOM에 동기화한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link BoxElementFactory} — TypeElement 생성 팩토리</li>
 *   <li>{@link TypeSearchProvider} — 현재 가시적인 타입 목록 구독</li>
 *   <li>{@link PositionMap} — 타입 위치 조회</li>
 *   <li>{@link ActionManager} — Move/Delete/ComplexAction 실행</li>
 *   <li>{@link SelectedBoxElement} — 선택 상태</li>
 *   <li>{@link DragShapeElement} — 드래그 시각 피드백</li>
 *   <li>{@link CanvasMode}, {@link GridSnap} — 모드/스냅 설정</li>
 *   <li>{@link CanvasContextMenuElement}, {@link BoxContextMenuElement} — 컨텍스트 메뉴</li>
 * </ul></p>
 * <p><b>주의:</b> elementMap으로 typeKey → TypeElement 매핑을 유지한다.
 * 가시성 필터링은 {@link TypeSearchProvider}에 위임하여 일관성을 확보한다.</p>
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
    private final TypeSearchProvider typeSearchProvider;
    private final TouchEventAdapter touchAdapter;
    private final PinchZoomHandler pinchZoom;
    private final VersionHistoryPanel versionHistoryPanel;
    private final LayoutProvider layoutProvider;
    private final Map<String, TypeElement> elementMap = new LinkedHashMap<>();

    private final CanvasShortcutHandler shortcutHandler;

    @Inject
    CanvasElement(BoxElementFactory boxFactory, TypeList typeList, ActionManager actionManager,
                  PositionMap positionMap, SelectedBoxElement selection, DragShapeElement dragShape,
                  BoxReferenceElement referenceElement, ChangeTracker tracker,
                  CanvasMode canvasMode, GridSnap gridSnap, TypeSearchProvider typeSearchProvider,
                  CanvasContextMenuElement canvasMenu, BoxContextMenuElement boxMenu,
                  TouchEventAdapter touchAdapter, PinchZoomHandler pinchZoom,
                  VersionHistoryPanel versionHistoryPanel, CanvasShortcutHandler shortcutHandler,
                  LayoutProvider layoutProvider) {
        this.shortcutHandler = shortcutHandler;
        this.canvasMode = canvasMode;
        this.gridSnap = gridSnap;
        this.typeSearchProvider = typeSearchProvider;
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
        this.versionHistoryPanel = versionHistoryPanel;
        this.layoutProvider = layoutProvider;

        dragShape.onDrop(delta -> {
            Set<String> selected = new HashSet<>(selection.getValue());
            if (!selected.isEmpty() && (delta[0] != 0 || delta[1] != 0)) {
                // 스냅: 첫 번째 선택 박스 기준으로 스냅 델타 계산
                int dx = delta[0], dy = delta[1];
                if (gridSnap.isEnabled()) {
                    String firstKey = selected.iterator().next();
                    Position firstPos = positionMap.get(firstKey);
                    if (firstPos != null) {
                        dx = gridSnap.snapDelta(firstPos.x(), dx);
                        dy = gridSnap.snapDelta(firstPos.y(), dy);
                    }
                }
                if (dx == 0 && dy == 0) return;
                
                Set<String> activeKeys = typeSearchProvider.getVisibleTypes().stream()
                        .map(Type::key).collect(Collectors.toSet());
                
                MoveBoxAction move = new MoveBoxAction(positionMap, layoutProvider, tracker, selected, dx, dy);
                Action[] pushOuts = selected.stream()
                        .map(key -> new PushOutOverlapAction(positionMap, key, 10, activeKeys))
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

        // 가시적인 타입 목록만 구독하여 DOM 동기화 (UC-T27 단일 가시성 소스)
        typeSearchProvider.visibleTypes().subscribe(this::syncElements);
    }

    private void syncElements(Set<Type> visibleTypes) {
        Set<String> currentKeys = new HashSet<>(elementMap.keySet());
        Set<String> newKeys = new HashSet<>();
        
        if (visibleTypes != null) {
            for (Type type : visibleTypes) {
                try {
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
                } catch (Throwable t) {
                    com.google.gwt.core.client.GWT.log("CanvasElement: error syncing box: " + t.getMessage(), t);
                }
            }
        }
        
        // 필터링되거나 제거된 요소 삭제
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
        canvasMode.getCurrentState().onCanvasKeyDown(e, () -> shortcutHandler.handle(new CanvasShortcutHandler.KeyboardInput() {
            @Override public String getKey() { return e.key; }
            @Override public boolean isCtrl() { return e.ctrlKey; }
            @Override public boolean isShift() { return e.shiftKey; }
            @Override public void preventDefault() { e.preventDefault(); }
        }));
    }

    public TypeElement getElement(String typeKey) {
        return elementMap.get(typeKey);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
