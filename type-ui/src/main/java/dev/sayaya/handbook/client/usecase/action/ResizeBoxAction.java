package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.Position;

/**
 * 타입 박스의 크기를 변경하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 PositionMap에 변경 후 위치(after)를 설정하고,
 * rollback 시 변경 전 위치(before)로 복원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link PositionMap} — 위치 설정</li>
 *   <li>{@link LayoutProvider} — 현재 레이아웃 정보 획득</li>
 *   <li>{@link ChangeTracker} — 레이아웃 변경 마킹</li>
 * </ul></p>
 * <p><b>주의:</b> TypeElement의 리사이즈 핸들에서 실시간 적용 후,
 * mouseup 시 undo용으로만 기록되는 경우가 있다(execute가 no-op인 익명 서브클래스).</p>
 */
public class ResizeBoxAction implements Action {
    private final PositionMap positionMap;
    private final LayoutProvider layoutProvider;
    private final ChangeTracker tracker;
    private final String typeKey;
    private final Position before;
    private final Position after;

    public ResizeBoxAction(PositionMap positionMap, LayoutProvider layoutProvider, ChangeTracker tracker,
                           String typeKey, Position before, Position after) {
        this.positionMap = positionMap;
        this.layoutProvider = layoutProvider;
        this.tracker = tracker;
        this.typeKey = typeKey;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() {
        positionMap.put(typeKey, after);
        markLayoutChanged();
    }

    @Override
    public void rollback() {
        positionMap.put(typeKey, before);
        markLayoutChanged();
    }

    private void markLayoutChanged() {
        if (layoutProvider != null && layoutProvider.getValue() != null) {
            tracker.markChanged("LAYOUT:" + layoutProvider.getValue().id());
        }
    }
}
