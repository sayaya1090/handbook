package dev.sayaya.handbook.client.interfaces.canvas;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeSearchProvider;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.client.usecase.action.MoveBoxAction;
import dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.Type;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 캔버스에서 발생하는 키보드 단축키를 해석하고 비즈니스 액션을 트리거하는 핸들러.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>Ctrl+Z / Ctrl+Shift+Z: Undo / Redo 실행</li>
 *   <li>Ctrl+A: 현재 가시적인 모든 타입 선택</li>
 *   <li>Delete / Backspace: 선택된 타입 삭제</li>
 *   <li>Arrow Keys: 선택된 타입 박스 이동 및 충돌 방지 로직 실행</li>
 * </ul></p>
 */
public class CanvasShortcutHandler {
    
    public interface KeyboardInput {
        String getKey();
        boolean isCtrl();
        boolean isShift();
        void preventDefault();
    }

    private final ActionManager actionManager;
    private final TypeList typeList;
    private final TypeSearchProvider typeSearchProvider;
    private final SelectedBoxElement selection;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final GridSnap gridSnap;
    private final LayoutProvider layoutProvider;

    @Inject
    public CanvasShortcutHandler(ActionManager actionManager, TypeList typeList,
                                 TypeSearchProvider typeSearchProvider, SelectedBoxElement selection,
                                 PositionMap positionMap, ChangeTracker tracker, GridSnap gridSnap,
                                 LayoutProvider layoutProvider) {
        this.actionManager = actionManager;
        this.typeList = typeList;
        this.typeSearchProvider = typeSearchProvider;
        this.selection = selection;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.gridSnap = gridSnap;
        this.layoutProvider = layoutProvider;
    }

    /** 키보드 이벤트를 처리한다. */
    public void handle(KeyboardInput e) {
        if (e.isCtrl() && "z".equals(e.getKey())) {
            e.preventDefault();
            if (e.isShift()) actionManager.redo();
            else actionManager.undo();
        } else if (e.isCtrl() && ("a".equals(e.getKey()) || "A".equals(e.getKey()))) {
            e.preventDefault();
            Set<String> allKeys = new HashSet<>();
            for (Type type : typeSearchProvider.getVisibleTypes()) {
                allKeys.add(type.key());
            }
            selection.selectAll(allKeys);
        } else if ("Delete".equals(e.getKey()) || "Backspace".equals(e.getKey())) {
            e.preventDefault();
            Set<String> selected = new HashSet<>(selection.getValue());
            for (Type type : typeSearchProvider.getVisibleTypes()) {
                if (selected.contains(type.key())) {
                    actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
                }
            }
            selection.clear();
        } else if (e.getKey() != null && e.getKey().startsWith("Arrow")) {
            e.preventDefault();
            Set<String> selected = selection.getValue();
            if (selected.isEmpty()) return;
            int step = gridSnap.isEnabled() ? 20 : (e.isShift() ? 20 : 5);
            int dx = 0, dy = 0;
            switch (e.getKey()) {
                case "ArrowUp":    dy = -step; break;
                case "ArrowDown":  dy = step;  break;
                case "ArrowLeft":  dx = -step; break;
                case "ArrowRight": dx = step;  break;
            }
            if (dx != 0 || dy != 0) {
                Set<String> activeKeys = typeSearchProvider.getVisibleTypes().stream()
                        .map(Type::key).collect(Collectors.toSet());
                
                Set<String> keys = new HashSet<>(selected);
                MoveBoxAction move = new MoveBoxAction(positionMap, layoutProvider, tracker, keys, dx, dy);
                Action[] pushOuts = keys.stream()
                        .map(key -> new PushOutOverlapAction(positionMap, key, 10, activeKeys))
                        .toArray(Action[]::new);
                Action[] all = new Action[1 + pushOuts.length];
                all[0] = move;
                System.arraycopy(pushOuts, 0, all, 1, pushOuts.length);
                actionManager.execute(new ComplexAction(all));
            }
        }
    }
}
