package dev.sayaya.handbook.client.usecase.action;


import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 변경/삭제된 타입과 레이아웃 위치를 서버에 저장한다.
 * 저장 후 ChangeTracker를 초기화한다.
 */
public class SaveAction implements Action {
    private final TypeRepository typeRepository;
    private final LayoutRepository layoutRepository;
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final ActionManager actionManager;
    private final LayoutProvider layoutProvider;

    public SaveAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                      TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                      ActionManager actionManager, LayoutProvider layoutProvider) {
        this.typeRepository = typeRepository;
        this.layoutRepository = layoutRepository;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.layoutProvider = layoutProvider;
    }

    @Override
    public void execute() {
        LayoutPeriod period = layoutProvider.getValue();
        if (period == null) return;

        Set<String> changedKeys = tracker.getChangedKeys();
        Set<String> deletedKeys = tracker.getDeletedKeys();

        // 변경된 타입 저장
        if (!changedKeys.isEmpty()) {
            Set<TypeValue> toSave = new HashSet<>();
            for (TypeValue type : typeList.getValue()) {
                if (changedKeys.contains(type.key())) toSave.add(type);
            }
            if (!toSave.isEmpty()) {
                typeRepository.save(toSave).subscribe(saved -> {});
            }
        }

        // 삭제된 타입 처리
        if (!deletedKeys.isEmpty()) {
            Set<TypeValue> toDelete = new HashSet<>();
            // 삭제된 타입은 이미 typeList에서 제거되었으므로 key로 임시 객체 생성
            for (String key : deletedKeys) {
                String[] parts = key.split(":");
                if (parts.length == 2) {
                    TypeValue dummy = TypeValue.create(parts[0], parts[1], 0, 0);
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
    }

    @Override
    public void rollback() {
        // 저장은 되돌릴 수 없음
    }
}
