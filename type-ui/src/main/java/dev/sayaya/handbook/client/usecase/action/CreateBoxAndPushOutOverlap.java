package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.BoxElement;
import dev.sayaya.handbook.client.usecase.BoxElementList;
import dev.sayaya.handbook.client.usecase.BoxList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateBoxAndPushOutOverlap extends ComplexAction {
    public CreateBoxAndPushOutOverlap(BoxList boxList, BoxElementList previous, Box box) {
        this(new CreateBoxAction(boxList, box), moveBox(box, previous));
    }
    private CreateBoxAndPushOutOverlap(CreateBoxAction createAction, MoveBoxAction[] actions) {
        super(Stream.concat(Stream.of(createAction), Arrays.stream(actions)).toArray(Action[]::new));
    }
    private static int[] calculateOverlap(Box boxA, Box boxB) {
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

    private static MoveBoxAction[] moveBox(Box box, BoxElementList boxList) {
        Map<BoxElement, Box> others = Arrays.stream(boxList.getValue()).collect(Collectors.toMap(e->e, BoxElement::toDomain));
        return moveBox(box, others, new ArrayList<>())
                .stream()
                .toArray(MoveBoxAction[]::new);
    }

    private static List<MoveBoxAction> moveBox(Box box, Map<BoxElement, Box> others, List<MoveBoxAction> actions) {
        Queue<Box> queue = new LinkedList<>();
        queue.add(box);
        while (!queue.isEmpty()) {
            Box currentBox = queue.poll();
            for (Map.Entry<BoxElement, Box> entry : others.entrySet()) {
                var otherBox = entry.getValue();
                if (otherBox.equals(currentBox)) continue; // 동일 박스 건너뜀

                var otherElement = entry.getKey();
                int[] overlap = calculateOverlap(currentBox, otherBox);
                if (overlap[0] != 0 || overlap[1] != 0) {
                    var action = new MoveBoxAction(otherElement, overlap[0], overlap[1]);
                    actions.add(action);
                    var nextBox = translate(otherBox, overlap[0], overlap[1]);
                    others.put(otherElement, nextBox);
                    queue.add(nextBox); // 이동한 박스를 큐에 추가하여 추후 다른 충돌 검사
                }
            }
        }
        return actions;
    }
    private static Box translate(Box origin, int dx, int dy) {
        return origin.toBuilder()
                .x(origin.x() + dx)
                .y(origin.y() + dy)
                .build();
    }
}
