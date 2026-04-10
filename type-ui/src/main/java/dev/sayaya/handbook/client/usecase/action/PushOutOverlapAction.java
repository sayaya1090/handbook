package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.usecase.PositionMap;

import java.util.*;

/**
 * 겹치는 타입 박스를 밀어낸다.
 * BFS 큐 기반으로 연쇄 충돌(A→B→C)을 해소하며,
 * 최소 이동 방향(상하좌우)을 선택하여 박스를 밀어낸다.
 */
public class PushOutOverlapAction implements Action {
    private final PositionMap positionMap;
    private final String sourceKey;
    private final int padding;
    private Map<String, Position> originalPositions;

    public PushOutOverlapAction(PositionMap positionMap, String sourceKey, int padding) {
        this.positionMap = positionMap;
        this.sourceKey = sourceKey;
        this.padding = padding;
    }

    @Override
    public void execute() {
        originalPositions = new HashMap<>(positionMap.getValue());
        calculate();
    }

    private void calculate() {
        Queue<String> queue = new LinkedList<>();
        Set<String> processed = new HashSet<>();
        queue.add(sourceKey);
        processed.add(sourceKey);

        while (!queue.isEmpty()) {
            String currentKey = queue.poll();
            Position current = positionMap.get(currentKey);
            if (current == null) continue;

            for (Map.Entry<String, Position> entry : new HashMap<>(positionMap.getValue()).entrySet()) {
                String otherKey = entry.getKey();
                if (otherKey.equals(currentKey)) continue;
                Position other = entry.getValue();
                int[] delta = calculateOverlap(current, other);
                if (delta == null) continue;
                positionMap.put(otherKey, Position.of(other.x + delta[0], other.y + delta[1], other.width, other.height));
                if (!processed.contains(otherKey)) {
                    processed.add(otherKey);
                    queue.add(otherKey);
                }
            }
        }
    }

    /**
     * 두 박스가 겹치면 최소 이동 방향(dx, dy)을 반환한다. 겹치지 않으면 null.
     */
    private int[] calculateOverlap(Position a, Position b) {
        int aRight = a.x + a.width;
        int aBottom = a.y + a.height;
        int bRight = b.x + b.width;
        int bBottom = b.y + b.height;

        if (a.x >= bRight || aRight <= b.x || a.y >= bBottom || aBottom <= b.y) return null;

        int pushLeft  = -(bRight - a.x + padding);
        int pushRight = aRight - b.x + padding;
        int pushUp    = -(bBottom - a.y + padding);
        int pushDown  = aBottom - b.y + padding;

        int minAbsX = Math.abs(pushLeft) <= Math.abs(pushRight) ? pushLeft : pushRight;
        int minAbsY = Math.abs(pushUp) <= Math.abs(pushDown) ? pushUp : pushDown;

        if (Math.abs(minAbsX) <= Math.abs(minAbsY)) return new int[]{minAbsX, 0};
        else return new int[]{0, minAbsY};
    }

    @Override
    public void rollback() {
        if (originalPositions != null) positionMap.replace(originalPositions);
    }
}
