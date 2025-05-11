package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Type;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

// 타입 변경 감지 및 Period 재계산
@Singleton
public class PeriodRecalculationService {
    static final Date MIN_DATE = new Date(0); // 극단적인 과거 (오버플로우 방지)
    static final Date MAX_DATE = new Date(32503680000000L); // 극단적인 미래 (오버플로우 방지)
    private final CalculatedLayoutProvider provider;
    @Inject PeriodRecalculationService(CalculatedLayoutProvider provider, TypeList types) {
        this.provider = provider;
        types.distinctUntilChanged().subscribe(this::recalculate);
    }
    private void recalculate(Set<Type> types) {
        var recalculated = recalculatePeriods(types);
        var prev = provider.getValue();
        if(!prev.equals(recalculated)) provider.next(recalculated);
    }
    private List<Period> recalculatePeriods(Collection<Type> allTypes) {
        if (allTypes == null || allTypes.isEmpty()) return Collections.emptyList();

        // 1. 모든 유효한 시간점(Date) 수집 및 정렬
        var sortedTimePoints = extractAndSortTimePoints(allTypes);
        if (sortedTimePoints.size() < 2) return List.of(createFullRangePeriod());

        // 2. 최소 단위 Period 생성 및 해당 Period에 활성화된 타입 매핑
        var intermediatePeriods = buildIntermediatePeriods(sortedTimePoints, allTypes);
        if (intermediatePeriods.isEmpty()) {
            // 이 경우는 sortedTimePoints.size() >= 2 이지만, 유효한 중간 Period가 생성되지 않은 경우.
            // (예: 모든 타입이 MIN_DATE ~ MAX_DATE에 걸쳐있고, 다른 시간점이 없는 경우)
            return List.of(createFullRangePeriod());
        }

        // 3. Period 병합
        var finalPeriods = mergeAndFinalizePeriods(intermediatePeriods);
        if (finalPeriods.isEmpty() && !allTypes.isEmpty())
            return List.of(createFullRangePeriod()); // 병합 후에도 최종 Period가 없고, 타입이 존재한다면 전체 기간 Period로 처리


        return Collections.unmodifiableList(finalPeriods);
    }
    private List<Date> extractAndSortTimePoints(Collection<Type> allTypes) {
        // Set을 사용하여 중복을 자동으로 제거하고, Comparator로 정렬
        SortedSet<Date> timePoints = new TreeSet<>(Comparator.comparingLong(Date::getTime));
        timePoints.add(new Date(MIN_DATE.getTime())); // 전체 범위의 시작점
        timePoints.add(new Date(MAX_DATE.getTime())); // 전체 범위의 끝점

        allTypes.forEach(type -> {
            if (type.effectDateTime() != null) timePoints.add(new Date(Math.max(0, type.effectDateTime().getTime())));
            if (type.expireDateTime() != null) {
                Date expireDate = new Date(type.expireDateTime().getTime());
                timePoints.add(expireDate.after(MAX_DATE) ? new Date(MAX_DATE.getTime()) : expireDate); // MAX_DATE로 클램핑
            }
        });
        return new ArrayList<>(timePoints);
    }
    private List<IntermediatePeriod> buildIntermediatePeriods(List<Date> sortedTimePoints, Collection<Type> allTypes) {
        List<IntermediatePeriod> intermediatePeriods = new ArrayList<>();
        for (int i = 0; i < sortedTimePoints.size() - 1; i++) {
            Date start = sortedTimePoints.get(i);
            Date end = sortedTimePoints.get(i + 1);
            if (!start.before(end)) continue; // 시작이 끝보다 같거나 이후면 유효하지 않은 구간

            var activeTypes = getActiveTypeKeysForPeriod(start, end, allTypes);
            intermediatePeriods.add(new IntermediatePeriod(start, end, activeTypes));
        }
        return intermediatePeriods;
    }
    private Set<Type> getActiveTypeKeysForPeriod(Date periodStart, Date periodEnd, Collection<Type> allTypes) {
        return allTypes.stream().filter(type -> {
                    Date typeEffect = (type.effectDateTime() != null) ? new Date(type.effectDateTime().getTime()) : new Date(MIN_DATE.getTime());
                    Date typeExpire = (type.expireDateTime() != null) ? new Date(type.expireDateTime().getTime()) : new Date(MAX_DATE.getTime());

                    // 타입의 유효 기간 [typeEffect, typeExpire)
                    // 현재 검사하는 최소 단위 Period [periodStart, periodEnd)
                    // 조건: typeEffect <= periodStart AND typeExpire > periodStart (즉, periodStart 시점에 타입이 활성)
                    //       AND typeExpire >= periodEnd (즉, 이 Period의 끝까지 타입이 유효)
                    // 더 정확한 겹침 조건: max(typeEffect, periodStart) < min(typeExpire, periodEnd)
                    // 여기서는 기존 로직을 유지: (typeEffect <= periodStart) && (typeExpire > periodStart) && (typeExpire >= periodEnd)
                    boolean startsBeforeOrAtPeriodStart = !typeEffect.after(periodStart); // typeEffect <= periodStart
                    boolean expiresAfterPeriodStart = typeExpire.after(periodStart);       // typeExpire > periodStart
                    boolean expiresAtOrAfterPeriodEnd = !typeExpire.before(periodEnd);   // typeExpire >= periodEnd

                    return startsBeforeOrAtPeriodStart && expiresAfterPeriodStart && expiresAtOrAfterPeriodEnd;
                }).collect(Collectors.toSet());
    }
    private List<Period> mergeAndFinalizePeriods(List<IntermediatePeriod> intermediatePeriods) {
        if (intermediatePeriods.isEmpty()) {
            return Collections.emptyList();
        }

        List<Period> finalPeriods = new ArrayList<>();
        IntermediatePeriod currentProcessingPeriod = new IntermediatePeriod(
                intermediatePeriods.get(0).start,
                intermediatePeriods.get(0).end,
                intermediatePeriods.get(0).activeTypes
        ); // 첫 번째 Period 복사해서 시작

        for (int i = 1; i < intermediatePeriods.size(); i++) {
            IntermediatePeriod nextPeriodToCompare = intermediatePeriods.get(i);

            // 현재 Period의 끝과 다음 Period의 시작이 정확히 일치하고, 활성 타입 목록도 같다면 병합
            if (currentProcessingPeriod.end.getTime() == nextPeriodToCompare.start.getTime() &&
                    currentProcessingPeriod.activeTypes.equals(nextPeriodToCompare.activeTypes)) {
                currentProcessingPeriod.end = new Date(nextPeriodToCompare.end.getTime()); // 현재 Period의 끝을 다음 Period의 끝으로 확장
            } else {
                // 병합할 수 없으므로, 현재까지 처리된 Period를 최종 목록에 추가
                if (currentProcessingPeriod.start.before(currentProcessingPeriod.end)) {
                    finalPeriods.add(createPeriodFromIntermediate(currentProcessingPeriod));
                }
                // 다음 Period를 현재 처리 대상으로 설정 (새로운 객체로 복사)
                currentProcessingPeriod = new IntermediatePeriod(
                        nextPeriodToCompare.start,
                        nextPeriodToCompare.end,
                        nextPeriodToCompare.activeTypes
                );
            }
        }
        // 마지막으로 처리된 Period 추가
        if (currentProcessingPeriod.start.before(currentProcessingPeriod.end)) {
            finalPeriods.add(createPeriodFromIntermediate(currentProcessingPeriod));
        }
        return finalPeriods;
    }
    private Period createPeriodFromIntermediate(IntermediatePeriod intermediatePeriod) {
        return Period.builder()
                .effectDateTime(new Date(intermediatePeriod.start.getTime())) // 방어적 복사
                .expireDateTime(new Date(intermediatePeriod.end.getTime()))   // 방어적 복사
                .build();
    }
    private Period createFullRangePeriod() {
        return Period.builder()
                .effectDateTime(new Date(MIN_DATE.getTime()))
                .expireDateTime(new Date(MAX_DATE.getTime()))
                .build();
    }
    private static class IntermediatePeriod {
        private final Date start;
        private Date end;
        private final Set<Type> activeTypes;

        private IntermediatePeriod(Date start, Date end, Set<Type> activeTypes) {
            // Date 객체의 불변성을 위해 방어적 복사
            this.start = new Date(start.getTime());
            this.end = new Date(end.getTime());
            this.activeTypes = new HashSet<>(activeTypes); // 리스트도 복사
        }
    }

}
