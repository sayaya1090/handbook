package dev.sayaya.handbook.client.usecase.action;


import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.*;

import java.util.Map;
import java.util.Set;

/**
 * 서버에서 타입과 레이아웃을 로드한다.
 * 로드 후 ActionManager의 undo/redo 스택을 초기화한다.
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
        // 레이아웃 목록 로드 → 기간 선택 → 타입 + 위치 로드
        layoutRepository.layouts().subscribe(periods -> {
            layoutList.next(periods);
            layoutProvider.selectBestMatch(periods);
            LayoutPeriod current = layoutProvider.getValue();
            if (current == null) return;
            typeRepository.list(current).subscribe(types -> {
                typeList.replace(types);
                tracker.reset();
                actionManager.clear();
            });
            layoutRepository.positions(current).subscribe(positions -> {
                positionMap.replace(positions);
            });
        });
    }

    @Override
    public void rollback() {
        // 로드는 되돌릴 수 없음 — 스택이 초기화되므로 undo 대상 아님
    }
}
