package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.UpdatableType;
import dev.sayaya.handbook.client.usecase.UpdatableTypeList;
import elemental2.dom.DomGlobal;

import java.util.*;
import java.util.stream.Collectors;

public class PushOutOverlapAction extends ComplexAction {
    // previous 영역에서 box의 이동 후 오버랩되는 box를 적절히 밀어낸다.
    @AssistedInject PushOutOverlapAction(UpdatableTypeList previous, MoveBoxAction.MoveBoxActionFactory moveBoxActionFactory, @Assisted Type... boxes) {
        this(calculate(
                boxes,
                Arrays.stream(previous.values()).collect(Collectors.toMap(e->e, UpdatableType::value)),
                new ArrayList<>(),
                moveBoxActionFactory
        ));
    }
    private PushOutOverlapAction(Action[] moves) {
        super(moves);
    }
    private static Action[] calculate(Type[] boxes, Map<UpdatableType, Type> others, List<Action> actions, MoveBoxAction.MoveBoxActionFactory moveBoxActionFactory) {
        Queue<Type> queue = new LinkedList<>();
        Collections.addAll(queue, boxes);
        Set<Type> processed = new HashSet<>();
        while (!queue.isEmpty()) {
            Type currentBox = queue.poll();
            for (Map.Entry<UpdatableType, Type> entry : others.entrySet()) {
                var otherBox = entry.getValue();
                if (otherBox.id().equals(currentBox.id())) continue; // 동일 박스 건너뜀
                if (processed.contains(otherBox)) continue;

                var otherElement = entry.getKey();
                int[] overlap = calculateOverlap(currentBox, otherBox);
                if (overlap[0] != 0 || overlap[1] != 0) {
                    var action = moveBoxActionFactory.move(otherElement, overlap[0], overlap[1]);
                    actions.add(action);
                    var nextBox = translate(otherBox, overlap[0], overlap[1]);
                    others.put(otherElement, nextBox);
                    queue.add(nextBox); // 이동한 박스를 큐에 추가하여 추후 다른 충돌 검사
                }
            }
            processed.add(currentBox);
        }

        return actions.stream().toArray(Action[]::new);
    }
    private static Type translate(Type origin, int dx, int dy) {
        return origin.toBuilder().x(origin.x() + dx).y(origin.y() + dy).build();
    }
    private static int[] calculateOverlap(Type boxA, Type boxB) {
        // boxA와 boxB의 각각의 경계
        int aRight = boxA.x() + boxA.width();
        int aBottom = boxA.y() + boxA.height();
        int bRight = boxB.x() + boxB.width();
        int bBottom = boxB.y() + boxB.height();
        // X축 및 Y축 겹침 계산
        boolean overlapX = !(aRight <= boxB.x() || boxA.x() >= bRight);
        boolean overlapY = !(aBottom <= boxB.y() || boxA.y() >= bBottom);
        if (!overlapX || !overlapY) return new int[]{0, 0};  // 겹침 여부 판단 (겹침이 없으면 바로 종료)
        // 겹치는 범위의 크기 계산
        int overlapLeft = aRight - boxB.x();
        int overlapRight = bRight - boxA.x();
        int overlapTop = aBottom - boxB.y();
        int overlapBottom = bBottom - boxA.y();
        int margin = 10;             // 마진 추가
        // 겹침 해소를 위한 이동 크기 계산
        int deltaX = (overlapLeft < overlapRight ? overlapLeft + margin : -overlapRight - margin);
        int deltaY = (overlapTop < overlapBottom ? overlapTop + margin : -overlapBottom - margin);

        // 더 작은 이동량만 선택
        return Math.abs(deltaX) > Math.abs(deltaY) ? new int[]{0, deltaY} : new int[]{deltaX, 0};
    }
    @AssistedFactory
    interface PushOutOverlapActionFactory {
        PushOutOverlapAction pushOutOverlap(Type... boxes);
    }
}
