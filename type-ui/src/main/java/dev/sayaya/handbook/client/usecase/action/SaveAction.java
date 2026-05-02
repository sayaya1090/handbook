package dev.sayaya.handbook.client.usecase.action;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.*;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import java.util.HashSet;
import java.util.Set;

/**
 * 변경/삭제된 타입과 레이아웃 위치를 서버에 저장하는 액션.
 *
 * <p><b>책임:</b> ChangeTracker에서 변경/삭제 키를 조회하여 변경된 타입은 save(),
 * 삭제된 타입은 delete(), 레이아웃 위치는 savePositions()로 서버에 전송한다.
 * 저장 후 ChangeTracker와 ActionManager를 초기화하고 성공 토스트를 표시한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeRepository} — 타입 저장/삭제 API</li>
 *   <li>{@link LayoutRepository} — 위치 저장 API</li>
 *   <li>{@link TypeList} — 변경된 타입 조회</li>
 *   <li>{@link PositionMap} — 위치 데이터</li>
 *   <li>{@link ChangeTracker} — 변경/삭제 키 조회 및 초기화</li>
 *   <li>{@link ActionManager} — Undo/Redo 스택 초기화</li>
 *   <li>{@link LayoutProvider} — 현재 기간 조회</li>
 *   <li>{@link ToastContainer} — 성공 피드백 토스트 표시</li>
 * </ul></p>
 * <p><b>주의:</b> rollback()은 no-op이다. 저장은 서버 상태를 변경하므로 되돌릴 수 없다.</p>
 */
public class SaveAction implements Action {
    private final TypeRepository typeRepository;
    private final LayoutRepository layoutRepository;
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final ActionManager actionManager;
    private final LayoutProvider layoutProvider;
    private final ToastContainer toastContainer;
    private final Labels labels;

    public SaveAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                      TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                      ActionManager actionManager, LayoutProvider layoutProvider) {
        this(typeRepository, layoutRepository, typeList, positionMap, tracker,
             actionManager, layoutProvider, null, Labels.empty());
    }

    public SaveAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                      TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                      ActionManager actionManager, LayoutProvider layoutProvider,
                      ToastContainer toastContainer, Labels labels) {
        this.typeRepository = typeRepository;
        this.layoutRepository = layoutRepository;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.layoutProvider = layoutProvider;
        this.toastContainer = toastContainer;
        this.labels = labels;
    }

    @Override
    public void execute() {
        LayoutPeriod period = layoutProvider.getValue();
        if (period == null) return;

        Set<String> changedKeys = tracker.getChangedKeys();
        Set<String> deletedKeys = tracker.getDeletedKeys();

        // 변경된 타입 저장
        if (!changedKeys.isEmpty()) {
            Set<Type> toSave = new HashSet<>();
            for (Type type : typeList.getValue()) {
                if (changedKeys.contains(type.key())) toSave.add(type);
            }
            if (!toSave.isEmpty()) {
                typeRepository.save(toSave).subscribe(saved -> {});
            }
        }

        // 삭제된 타입 처리
        if (!deletedKeys.isEmpty()) {
            Set<Type> toDelete = new HashSet<>();
            // 삭제된 타입은 이미 typeList에서 제거되었으므로 key로 임시 객체 생성
            for (String key : deletedKeys) {
                String[] parts = key.split(":");
                if (parts.length == 2) {
                    Type dummy = Type.create(parts[0], parts[1], 0.0, 0.0);
                    toDelete.add(dummy);
                }
            }
            if (!toDelete.isEmpty()) {
                typeRepository.delete(toDelete).subscribe(v -> {});
            }
        }

        // 레이아웃 위치 저장
        layoutRepository.savePositions(period, positionMap.getValue()).subscribe(v -> {});

        // 상태 초기화
        tracker.reset();
        actionManager.clear();

        // 성공 피드백 토스트
        if (toastContainer != null) {
            toastContainer.show(ToastLevel.SUCCESS,
                    labels.getOrDefault("toast.save.success", "Save completed"));
        }
    }

    @Override
    public void rollback() {
        // 저장은 되돌릴 수 없음
    }
}
