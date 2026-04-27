package dev.sayaya.handbook.client.usecase;

import javax.inject.Inject;
import javax.inject.Singleton;

/** 그리드 스냅 설정. 활성화 시 이동/드롭 좌표를 격자 크기에 맞춘다. */
@Singleton
public class GridSnap {
    private static final int GRID_SIZE = 20;
    private final dev.sayaya.rx.subject.BehaviorSubject<Boolean> enabled = dev.sayaya.rx.subject.BehaviorSubject.behavior(false);

    @Inject GridSnap() {}

    public boolean isEnabled() { return enabled.getValue(); }
    public void setEnabled(boolean enabled) { this.enabled.next(enabled); }
    public dev.sayaya.rx.Observable<Boolean> enabled() { return enabled.asObservable(); }

    public int snap(int value) {
        if (!isEnabled()) return value;
        return Math.round((float) value / GRID_SIZE) * GRID_SIZE;
    }

    public int snapDelta(int currentPos, int delta) {
        if (!isEnabled()) return delta;
        int target = currentPos + delta;
        int snapped = Math.round((float) target / GRID_SIZE) * GRID_SIZE;
        return snapped - currentPos;
    }
}
