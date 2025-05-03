package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.usecase.action.ActionFactory;
import dev.sayaya.handbook.client.usecase.action.ChangeLayoutAction;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class LayoutActionManager {
    private final ActionFactory factory;
    private final LayoutProvider layout;
    private final LayoutList layoutList;
    @Inject LayoutActionManager(ActionFactory factory, LayoutProvider layout, LayoutList layoutList) {
        this.factory = factory;
        this.layout = layout;
        this.layoutList = layoutList;
    }
    public boolean hasBeforeLayout() {
        return layoutList.findIndex(layout.getValue()) > 0;
    }
    /**
     * 현재 basetime을 basetimes 리스트의 이전 시간으로 변경합니다.
     * 현재 시간이 리스트에 없거나 가장 오래된 시간이면 변경하지 않습니다.
     */
    ChangeLayoutAction changeToBeforeLayout() {
        int currentIndex = layoutList.findIndex(layout.getValue());
        if (currentIndex > 0) return factory.changeLayout(layoutList.get(currentIndex - 1)); // 현재 인덱스가 0보다 크면 (첫 번째 요소가 아니면) 이전 요소로 업데이트
        else return null; // currentIndex가 -1 (리스트에 없음)이거나 0 (첫 요소)이면 변경하지 않음
    }
    public boolean hasAfterLayout() {
        int currentIndex = layoutList.findIndex(layout.getValue());
        return currentIndex >= 0 && currentIndex < layoutList.size() - 1;
    }
    ChangeLayoutAction changeToLayout(Period period) {
        return factory.changeLayout(period);
    }
    /**
     * 현재 basetime을 basetimes 리스트의 다음 시간으로 변경합니다.
     * 현재 시간이 리스트에 없거나 가장 최신 시간이면 변경하지 않습니다.
     */
    ChangeLayoutAction changeToAfterLayout() {
        int currentIndex = layoutList.findIndex(layout.getValue());
        if (currentIndex >= 0 && currentIndex < layoutList.size() - 1) return factory.changeLayout(layoutList.get(currentIndex + 1)); // 현재 인덱스가 유효하고 마지막 요소가 아니면 다음 요소로 업데이트
        else return null; // currentIndex가 -1 (리스트에 없음)이거나 마지막 요소면 변경하지 않음
    }
}
