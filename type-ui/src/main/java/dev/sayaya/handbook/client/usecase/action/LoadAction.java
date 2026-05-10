package dev.sayaya.handbook.client.usecase.action;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.TypeLayout;

import java.util.HashMap;
import java.util.Map;

/** 
 * 서버에서 전체 레이아웃 목록과 현재 레이아웃의 데이터를 로드한다.
 */
public class LoadAction implements Action {
    private final TypeRepository typeRepository;
    private final LayoutRepository layoutRepository;
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final ActionManager actionManager;
    private final LayoutProvider layoutProvider;
    private final LayoutList layoutList;

    public LoadAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                      TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                      ActionManager actionManager, LayoutProvider layoutProvider, LayoutList layoutList) {
        this.typeRepository = typeRepository;
        this.layoutRepository = layoutRepository;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.layoutProvider = layoutProvider;
        this.layoutList = layoutList;
    }

    @Override
    public void execute() {
        layoutRepository.layouts().subscribe(periods -> {
            if (periods == null || periods.isEmpty()) {
                TypeLayout defaultPeriod = TypeLayout.create(null, null, 0, 253402214400000.0, null);
                layoutList.replace(java.util.List.of(defaultPeriod));
                layoutProvider.replace(defaultPeriod);
            } else {
                layoutList.replace(periods);
                layoutProvider.selectBestMatch(periods);
            }

            TypeLayout current = layoutProvider.getValue();
            typeRepository.list(current.toPeriod()).subscribe(types -> {
                if (types != null) {
                    typeList.replace(types);
                    tracker.reset();
                    actionManager.clear();
                }
            });
            layoutRepository.positions(current.toPeriod()).subscribe(positions -> {
                positionMap.replace(positions);
            });
        });
    }

    @Override
    public void rollback() {
        // 로드는 되돌릴 수 없음
    }
}
