package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.domain.TypeLayout;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** 
 * 타입의 유효기간 변경 시 레이아웃 목록을 재계산하는 서비스.
 */
@Singleton
public class PeriodRecalculationService {
    private final TypeList typeList;
    private final LayoutList layoutList;
    private final LayoutProvider layoutProvider;

    @Inject
    PeriodRecalculationService(TypeList typeList, LayoutList layoutList, LayoutProvider layoutProvider) {
        this.typeList = typeList;
        this.layoutList = layoutList;
        this.layoutProvider = layoutProvider;

        typeList.subscribe(this::recalculate);
    }

    private void recalculate(Set<Type> types) {
        // 모든 타입의 시점을 수집
        TreeSet<Double> timePoints = new TreeSet<>();
        timePoints.add(0.0);
        timePoints.add(253402214400000.0);
        
        for (Type type : types) {
            timePoints.add(type.effectDateTime());
            timePoints.add(type.expireDateTime());
        }

        // 연속된 시점 쌍으로 기간 생성
        List<TypeLayout> oldLayouts = layoutList.getValue();
        List<TypeLayout> newLayouts = new ArrayList<>();
        Double prev = null;
        for (Double point : timePoints) {
            if (prev != null && !prev.equals(point)) {
                TypeLayout match = findMatch(oldLayouts, prev, point);
                if (match != null) {
                    TypeLayout newLayout = TypeLayout.create(match.id(), match.workspace(), prev, point, match.positions());
                    newLayout.rev(match.rev()); // 중요: 낙관적 잠금을 위해 기존 리비전 유지
                    newLayouts.add(newLayout);
                } else {
                    newLayouts.add(TypeLayout.create(null, null, prev, point, null));
                }
            }
            prev = point;
        }

        if (!newLayouts.isEmpty()) {
            layoutList.replace(newLayouts);
            layoutProvider.selectBestMatch(newLayouts);
        }
    }

    private TypeLayout findMatch(List<TypeLayout> layouts, double start, double end) {
        if (layouts == null) return null;
        for (TypeLayout layout : layouts) {
            if (Math.abs(layout.effectDateTime() - start) < 0.1 && Math.abs(layout.expireDateTime() - end) < 0.1) return layout;
        }
        return null;
    }
}
