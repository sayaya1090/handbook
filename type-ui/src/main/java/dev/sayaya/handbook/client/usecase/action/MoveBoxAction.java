package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.domain.Action;

import java.util.Set;

/**
 * 선택된 타입 박스들을 캔버스에서 이동하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 지정된 타입 키 집합의 위치를 (dx, dy)만큼 이동한다.
 * rollback 시 (-dx, -dy)로 역이동하여 원래 위치로 복원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link PositionMap} — 타입 위치 이동</li>
 *   <li>{@link LayoutProvider} — 현재 레이아웃 정보 획득</li>
 *   <li>{@link ChangeTracker} — 레이아웃 변경 마킹</li>
 * </ul></p>
 * <p><b>주의:</b> 다중 선택 시 모든 선택된 박스가 동일한 델타로 이동한다.
 * 보통 {@link PushOutOverlapAction}과 {@link ComplexAction}으로 묶여 겹침을 해소한다.</p>
 */
public class MoveBoxAction implements Action {
    private final PositionMap positionMap;
    private final LayoutProvider layoutProvider;
    private final ChangeTracker tracker;
    private final Set<String> typeKeys;
    private final int dx;
    private final int dy;

    public MoveBoxAction(PositionMap positionMap, LayoutProvider layoutProvider, ChangeTracker tracker,
                         Set<String> typeKeys, int dx, int dy) {
        this.positionMap = positionMap;
        this.layoutProvider = layoutProvider;
        this.tracker = tracker;
        this.typeKeys = typeKeys;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        typeKeys.forEach(key -> {
            dev.sayaya.handbook.domain.Position before = positionMap.get(key);
            if (before == null) before = dev.sayaya.handbook.domain.Position.of(20, 20, 240, 160);
            positionMap.move(key, dx, dy);
            dev.sayaya.handbook.domain.Position after = positionMap.get(key);
            tracker.trackChange(key + ":position", before, after, this::isSamePosition);
        });
        markLayoutChanged();
    }

    @Override
    public void rollback() {
        typeKeys.forEach(key -> {
            dev.sayaya.handbook.domain.Position before = positionMap.get(key); // 이건 after 위치
            positionMap.move(key, -dx, -dy);
            dev.sayaya.handbook.domain.Position after = positionMap.get(key); // 이건 복원된 before 위치
            tracker.trackChange(key + ":position", after, after, this::isSamePosition); // 원상 복구
        });
        markLayoutChanged();
    }

    private boolean isSamePosition(dev.sayaya.handbook.domain.Position b, dev.sayaya.handbook.domain.Position a) {
        if (b == a) return true;
        if (b == null || a == null) return false;
        return b.x() == a.x() && b.y() == a.y() && b.width() == a.width() && b.height() == a.height();
    }

    private void markLayoutChanged() {
        if (layoutProvider != null && layoutProvider.getValue() != null) {
            tracker.markChanged("LAYOUT:" + layoutProvider.getValue().id());
        }
    }
}
