package dev.sayaya.handbook.client.usecase.action;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.*;
import jsinterop.base.JsPropertyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 변경 사항을 하나의 SchemaPatch로 묶어 서버에 원자적으로 저장하는 액션.
 */
public class SaveAction implements Action {
    private final TypeRepository typeRepository;
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final ActionManager actionManager;
    private final LayoutProvider layoutProvider;
    private final LayoutList layoutList;
    private final ToastContainer toastContainer;
    private final Labels labels;

    public SaveAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                      TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                      ActionManager actionManager, LayoutProvider layoutProvider,
                      LayoutList layoutList, ToastContainer toastContainer, Labels labels) {
        this.typeRepository = typeRepository;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.layoutProvider = layoutProvider;
        this.layoutList = layoutList;
        this.toastContainer = toastContainer;
        this.labels = labels;
    }

    @Override
    public void execute() {
        Set<String> changedKeys = tracker.getChangedKeys();
        Set<String> deletedKeys = tracker.getDeletedKeys();
        
        List<SchemaPatch.TypeOperation> typeOps = new ArrayList<>();
        
        // 1. 삭제된 타입 처리
        for (String key : deletedKeys) {
            if (key.startsWith("LAYOUT:")) continue; // 레이아웃 삭제는 별도 로직 (현재 미지원)
            Type deletedType = tracker.getDeletedPayload(key);
            if (deletedType != null) {
                typeOps.add(SchemaPatch.TypeOperation.delete(deletedType));
            } else {
                String[] parts = key.split(":");
                if (parts.length == 2) {
                    Type dummy = Type.create(parts[0], parts[1], 0.0, 0.0);
                    typeOps.add(SchemaPatch.TypeOperation.delete(dummy));
                }
            }
        }
        
        // 2. 변경된 타입 처리
        for (Type type : typeList.getValue()) {
            if (changedKeys.contains(type.key())) {
                typeOps.add(SchemaPatch.TypeOperation.upsert(type));
            }
        }
        
        // 3. 레이아웃 처리 (변경된 것만 선택적으로 포함)
        List<SchemaPatch.LayoutOperation> layoutOps = new ArrayList<>();
        for (TypeLayout layout : layoutList.getValue()) {
            if (changedKeys.contains("LAYOUT:" + layout.id())) {
                JsPropertyMap<Position> posMap = JsPropertyMap.of();
                positionMap.getValue().forEach(posMap::set);
                
                // 기존 rev 유지 필수 (낙관적 잠금)
                TypeLayout toSave = TypeLayout.create(layout.id(), layout.workspace(), layout.effectDateTime(), layout.expireDateTime(), posMap);
                toSave.rev(layout.rev());
                layoutOps.add(SchemaPatch.LayoutOperation.upsert(toSave));
            }
        }

        // 아무 변경 사항이 없으면 무시
        if (typeOps.isEmpty() && layoutOps.isEmpty()) {
            if (toastContainer != null) {
                toastContainer.show(ToastLevel.INFO, labels.getOrDefault("toast.save.no_changes", "No changes to save"));
            }
            return;
        }

        SchemaPatch patch = SchemaPatch.create(typeOps.toArray(new SchemaPatch.TypeOperation[0]), layoutOps.toArray(new SchemaPatch.LayoutOperation[0]));
        
        typeRepository.patchSchema(patch).subscribe(result -> {
            // 트래커와 액션 매니저를 먼저 초기화하여 재렌더링 시 하이라이트가 제거되도록 함
            tracker.reset();
            actionManager.clear();

            // 서버에서 반환된 최신 데이터로 UI 상태 동기화 (리비전 갱신)
            if (result.types() != null) {
                for (SchemaPatch.TypeOperation op : result.types()) {
                    if ("UPSERT".equals(op.op())) {
                        typeList.update(op.data(), op.data()); // 리비전 갱신
                    }
                }
            }
            if (result.layouts() != null) {
                for (SchemaPatch.LayoutOperation op : result.layouts()) {
                    if ("UPSERT".equals(op.op())) {
                        layoutList.update(op.data(), op.data()); // 리비전 갱신
                        
                        // 현재 사용 중인 레이아웃이라면 LayoutProvider에도 즉시 동기화 (연속 저장 성공을 위해 필수)
                        TypeLayout current = layoutProvider.getValue();
                        if (current != null && current.id().equals(op.data().id())) {
                            layoutProvider.replace(op.data());
                        }
                    }
                }
            }
            
            if (toastContainer != null) {
                toastContainer.show(ToastLevel.SUCCESS, labels.getOrDefault("toast.save.success", "Save completed"));
            }
        });
    }

    @Override
    public void rollback() {
        // 저장은 되돌릴 수 없음
    }
}
