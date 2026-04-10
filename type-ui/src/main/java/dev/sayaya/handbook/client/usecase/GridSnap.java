package dev.sayaya.handbook.client.usecase;

import javax.inject.Inject;
import javax.inject.Singleton;

/** 그리드 스냅 설정. 활성화 시 이동/드롭 좌표를 격자 크기에 맞춘다. */
@Singleton
public class GridSnap {
    private static final int GRID_SIZE = 20;
    private boolean enabled = false;

    @Inject GridSnap() {}

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int snap(int value) {
        if (!enabled) return value;
        return Math.round((float) value / GRID_SIZE) * GRID_SIZE;
    }

    public int snapDelta(int currentPos, int delta) {
        if (!enabled) return delta;
        int target = currentPos + delta;
        int snapped = Math.round((float) target / GRID_SIZE) * GRID_SIZE;
        return snapped - currentPos;
    }
}
