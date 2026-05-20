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
import dev.sayaya.handbook.client.usecase.IntegrityAnalysisService;
import dev.sayaya.handbook.domain.*;
import jsinterop.base.JsPropertyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final IntegrityAnalysisService integrityAnalysisService;
    private final ToastContainer toastContainer;
    private final Labels labels;

    public SaveAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                      TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                      ActionManager actionManager, LayoutProvider layoutProvider,
                      LayoutList layoutList, IntegrityAnalysisService integrityAnalysisService, 
                      ToastContainer toastContainer, Labels labels) {
        this.typeRepository = typeRepository;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.layoutProvider = layoutProvider;
        this.layoutList = layoutList;
        this.integrityAnalysisService = integrityAnalysisService;
        this.toastContainer = toastContainer;
        this.labels = labels;
    }

    private void extractReferences(AttributeType attrType, Set<String> refs) {
        if (attrType == null) return;
        if ("document".equals(attrType.type()) && attrType.referencedType() != null) {
            refs.add(attrType.referencedType());
        }
        extractReferences(attrType.elementType(), refs);
        extractReferences(attrType.keyType(), refs);
        extractReferences(attrType.valueType(), refs);
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
        
        // 2. 저장 전 참조 정합성 교차 검증 (변경된 타입 중심의 정방향/역방향 검증 최적화)
        Set<String> changedTypeIds = new java.util.HashSet<>();
        for (Type type : typeList.getValue()) {
            if (changedKeys.contains(type.key())) {
                changedTypeIds.add(type.id());
            }
        }

        for (Type type : typeList.getValue()) {
            boolean isChanged = changedKeys.contains(type.key());
            if (type.attributes() != null) {
                Set<String> refs = new java.util.HashSet<>();
                for (Attribute attr : type.attributes()) {
                    extractReferences(attr.type(), refs);
                }
                for (String refId : refs) {
                    // 변경된 타입이 참조하는 대상(정방향)이거나, 캔버스 내 타 타입이 변경된 타입을 참조하는 경우(역방향)에만 검증
                    if (isChanged || changedTypeIds.contains(refId)) {
                        IntegrityAnalysisService.AnalysisResult res = integrityAnalysisService.analyze(type, refId);
                        if (!res.valid()) {
                            if (toastContainer != null) {
                                String msg = labels.getOrDefault("type.conflict.message", "The referenced type '{id}' is only available from {start} to {end}.")
                                        .replace("{id}", res.refId())
                                        .replace("{start}", res.coverageStart() == -1 ? "N/A" : dev.sayaya.handbook.client.usecase.DateFormatter.format(res.coverageStart()))
                                        .replace("{end}", res.coverageEnd() == -1 ? "N/A" : dev.sayaya.handbook.client.usecase.DateFormatter.format(res.coverageEnd()));
                                toastContainer.show(ToastLevel.ERROR, labels.getOrDefault("toast.save.error.integrity", "Integrity check failed") + ": " + msg);
                            }
                            return; // 저장 중단
                        }
                    }
                }
            }
        }
        
        // 3. 변경된 타입 추출
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
                Map<String, Position> allPositions = positionMap.getValue();
                
                // 해당 레이아웃 기간에 활성 상태인 타입들만 선별하여 위치 정보 저장
                Set<Type> allTypes = typeList.getValue();
                for (Type type : allTypes) {
                    if (type.effectDateTime() < layout.expireDateTime() && type.expireDateTime() > layout.effectDateTime()) {
                        Position pos = allPositions.get(type.key());
                        if (pos != null) {
                            posMap.set(type.key(), pos);
                        }
                    }
                }
                
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
            // 1. 트래커와 액션 매니저 초기화 (하이라이트 제거 준비)
            tracker.reset();
            actionManager.clear();

            // 2. 타입 동기화 (서버에서 반환된 최신 데이터로 교체)
            if (result.types() != null) {
                java.util.Set<Type> nextTypes = new java.util.LinkedHashSet<>(typeList.getValue());
                for (SchemaPatch.TypeOperation op : result.types()) {
                    if ("UPSERT".equals(op.op())) {
                        Type updated = op.data();
                        // 기존 목록에서 동일한 ID:Version을 찾아 최신 객체(rev 포함)로 교체
                        nextTypes.removeIf(t -> t.key().equals(updated.key()));
                        nextTypes.add(updated);
                    }
                }
                typeList.replace(nextTypes);
            }

            // 3. 레이아웃 동기화 (서버에서 반환된 최신 데이터로 교체)
            if (result.layouts() != null) {
                java.util.List<TypeLayout> nextLayouts = new java.util.ArrayList<>(layoutList.getValue());
                for (SchemaPatch.LayoutOperation op : result.layouts()) {
                    if ("UPSERT".equals(op.op())) {
                        TypeLayout updated = op.data();
                        // 3.1. 기존 목록에서 매칭되는 레이아웃 찾아 교체 (ID 또는 기간 기반)
                        boolean matched = false;
                        for (int i = 0; i < nextLayouts.size(); i++) {
                            TypeLayout existing = nextLayouts.get(i);
                            if (existing.id() != null && existing.id().equals(updated.id())) {
                                nextLayouts.set(i, updated);
                                matched = true;
                                break;
                            } else if (existing.id() == null && 
                                     Math.abs(existing.effectDateTime() - updated.effectDateTime()) < 0.1 && 
                                     Math.abs(existing.expireDateTime() - updated.expireDateTime()) < 0.1) {
                                // ID가 없던 신규 항목은 기간으로 매칭하여 ID가 부여된 최신 객체로 대체
                                nextLayouts.set(i, updated);
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) nextLayouts.add(updated);
                        
                        // 3.2. 현재 활성 레이아웃인 경우 Provider도 즉시 갱신 (연속 저장 보장)
                        TypeLayout current = layoutProvider.getValue();
                        if (current != null && (
                            (current.id() != null && current.id().equals(updated.id())) ||
                            (current.id() == null && Math.abs(current.effectDateTime() - updated.effectDateTime()) < 0.1 && Math.abs(current.expireDateTime() - updated.expireDateTime()) < 0.1)
                        )) {
                            layoutProvider.replace(updated);
                        }
                    }
                }
                layoutList.replace(nextLayouts);
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
