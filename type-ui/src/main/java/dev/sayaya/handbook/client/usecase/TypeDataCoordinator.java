package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.domain.TypeLayout;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * 레이아웃 전환 시 서버에서 필요한 데이터를 자동으로 로드하는 코디네이터.
 * 
 * <p><b>책임:</b>
 * 1. {@link LayoutProvider}를 구독하여 현재 레이아웃이 변경될 때마다 해당 기간의 타입과 위치 정보를 서버에서 가져온다.
 * 2. 중복 요청을 방지하기 위해 이미 로드된 기간(Layout ID 기준)을 추적한다.
 * 3. 서버에서 받은 데이터를 {@link TypeList}와 {@link PositionMap}에 머지한다.
 * </p>
 */
@Singleton
public class TypeDataCoordinator {
    private final TypeRepository typeRepository;
    private final LayoutRepository layoutRepository;
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final LayoutProvider layoutProvider;
    private final Set<String> loadedLayoutIds = new HashSet<>();

    @Inject
    TypeDataCoordinator(TypeRepository typeRepository, LayoutRepository layoutRepository,
                        TypeList typeList, PositionMap positionMap, LayoutProvider layoutProvider) {
        this.typeRepository = typeRepository;
        this.layoutRepository = layoutRepository;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.layoutProvider = layoutProvider;
    }

    public void init() {
        layoutProvider.subscribe(this::onLayoutChanged);
    }

    private void onLayoutChanged(TypeLayout layout) {
        if (layout == null) return;

        // 이미 로드된 레이아웃(ID 기준)이거나, 신규 생성 중인 레이아웃(ID 없음)인 경우 스킵
        if (layout.id() == null || loadedLayoutIds.contains(layout.id())) return;

        // 타입 목록 조회 및 머지
        typeRepository.list(layout.toPeriod()).subscribe(types -> {
            if (types != null) {
                typeList.merge(types);
                if (layout.id() != null) loadedLayoutIds.add(layout.id());
            }
        });

        // 위치 정보 조회 및 머지
        layoutRepository.positions(layout.toPeriod()).subscribe(positions -> {
            if (positions != null) {
                positionMap.merge(positions);
            }
        });
    }

    /** 
     * 로드된 캐시를 초기화한다. 새로고침(Reload) 시 사용됨.
     */
    public void clearCache() {
        loadedLayoutIds.clear();
    }
}
