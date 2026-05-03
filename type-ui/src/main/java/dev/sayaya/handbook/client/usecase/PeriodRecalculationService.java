package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Type;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 타입 목록의 effectDateTime/expireDateTime으로부터 유효 기간(LayoutPeriod) 목록을 재계산한다.
 * 타입이 추가/삭제/수정될 때 호출하여 LayoutList를 갱신한다.
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

        typeList.subscribe(types -> recalculate(types));
    }

    private void recalculate(Set<Type> types) {
        if (types.isEmpty()) return;

        // 모든 타입의 시점을 수집
        TreeSet<Double> timePoints = new TreeSet<>();
        for (Type type : types) {
            timePoints.add(type.effectDateTime());
            timePoints.add(type.expireDateTime());
        }

        // 연속된 시점 쌍으로 기간 생성
        List<LayoutPeriod> periods = new ArrayList<>();
        Double prev = null;
        for (Double point : timePoints) {
            if (prev != null && !prev.equals(point)) {
                periods.add(LayoutPeriod.of(prev, point));
            }
            prev = point;
        }

        if (!periods.isEmpty()) {
            layoutList.replace(periods);
            layoutProvider.selectBestMatch(periods);
        }
    }
}
