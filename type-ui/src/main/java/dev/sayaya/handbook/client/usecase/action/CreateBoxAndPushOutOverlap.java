package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.BoxElement;
import dev.sayaya.handbook.client.usecase.BoxElementList;
import dev.sayaya.handbook.client.usecase.BoxList;
import java.util.Random;

import java.util.*;
import java.util.stream.Stream;

public class CreateBoxAndPushOutOverlap extends ComplexAction {
    private static final Random random = new Random();
    public CreateBoxAndPushOutOverlap(BoxList boxList, BoxElementList previous, double x, double y) {
        this(new CreateBoxAction(boxList, x, y), moveBox(new Box("", "", (int)x, (int)y, 100, 100), previous));
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

        // 겹침 여부 판단
        if (aRight <= boxB.x() || boxA.x() >= bRight || aBottom <= boxB.y() || boxA.y() >= bBottom) {
            return new int[] {0, 0}; // 겹침 없음
        }

        // 겹치는 각 방향의 크기 계산
        int overlapLeft = aRight - boxB.x();
        int overlapRight = bRight - boxA.x();
        int overlapTop = aBottom - boxB.y();
        int overlapBottom = bBottom - boxA.y();

        // X축과 Y축의 이동 크기 결정 (10 ~ 20 사이의 마진 추가)
        int margin = random.nextInt(11) + 10;
        int deltaX = (overlapLeft < overlapRight ? overlapLeft + margin : -overlapRight - margin);
        int deltaY = (overlapTop < overlapBottom ? overlapTop + margin : -overlapBottom - margin);

        // X와 Y 중 더 작은 쪽으로만 이동
        if (Math.abs(deltaX) > Math.abs(deltaY)) return new int[] {0, deltaY};
        else return new int[] {deltaX, 0};
    }

    private static MoveBoxAction[] moveBox(Box box, BoxElementList boxList) {
        return moveBox(box, new LinkedList<>(Arrays.asList(boxList.getValue())), new ArrayList<>(), new HashSet<>())
                .stream()
                .toArray(MoveBoxAction[]::new);
    }

    private static List<MoveBoxAction> moveBox(Box box, LinkedList<BoxElement> boxList, List<MoveBoxAction> actions, Set<Box> processedBoxes) {
        // 이미 처리된 박스는 건너뜀
        if (processedBoxes.contains(box)) return actions;
        processedBoxes.add(box);
        Queue<Box> queue = new LinkedList<>();
        queue.add(box);
        while (!queue.isEmpty()) {
            Box currentBox = queue.poll();
            for (BoxElement other : boxList) {
                if (other.toDomain() == currentBox || processedBoxes.contains(other.toDomain())) continue; // 동일 객체 또는 이미 처리된 박스 건너뜀
                int[] overlap = calculateOverlap(currentBox, other.toDomain());
                if (overlap[0] != 0 || overlap[1] != 0) {
                    var action = new MoveBoxAction(other, overlap[0], overlap[1]);
                    actions.add(action);
                    //action.execute(); // 겹침 해결을 위해 실행
                    // 새로운 박스를 큐에 추가하여 추후 처리
                    queue.add(other.toDomain());
                    processedBoxes.add(other.toDomain());
                }
            }
        }
        return actions;
    }
}
