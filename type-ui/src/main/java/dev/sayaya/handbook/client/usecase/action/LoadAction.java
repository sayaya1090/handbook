package dev.sayaya.handbook.client.usecase.action;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.usecase.TypeRepository;

/**
 * 서버에서 타입과 레이아웃을 로드하는 액션.
 *
 * <p><b>책임:</b> 레이아웃 기간 목록을 로드하고, 현재 선택과 가장 겹치는 기간을 자동 선택한 뒤,
 * 해당 기간의 타입 목록과 위치 데이터를 로드한다.
 * 로드 후 ChangeTracker와 ActionManager를 초기화한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link LayoutRepository} — 기간 목록/위치 조회 API</li>
 *   <li>{@link TypeRepository} — 타입 목록 조회 API</li>
 *   <li>{@link LayoutList} — 기간 목록 상태 갱신</li>
 *   <li>{@link LayoutProvider} — 기간 자동 선택</li>
 *   <li>{@link TypeList} — 타입 목록 교체</li>
 *   <li>{@link PositionMap} — 위치 교체</li>
 *   <li>{@link ChangeTracker}, {@link ActionManager} — 초기화</li>
 * </ul></p>
 * <p><b>주의:</b> rollback()은 no-op이다. 로드 시 Undo/Redo 스택이 초기화되므로 undo 대상이 아니다.</p>
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
            layoutList.replace(periods);
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
