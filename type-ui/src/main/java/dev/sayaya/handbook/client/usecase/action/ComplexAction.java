package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.domain.Action;

import java.util.Arrays;
import java.util.List;

/**
 * 여러 액션을 하나의 트랜잭션으로 묶는 복합 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 포함된 액션을 순서대로 실행하고,
 * rollback 시 역순으로 롤백하여 원자적 Undo를 보장한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link Action} — 포함되는 개별 액션</li>
 * </ul></p>
 * <p><b>주의:</b> CreateBox + PushOutOverlap, Move + PushOutOverlap 등
 * 연관 액션을 묶어 단일 Undo 단위로 관리하는 데 사용된다.</p>
 */
public class ComplexAction implements Action {
    private final List<Action> actions;

    public ComplexAction(Action... actions) {
        this.actions = Arrays.asList(actions);
    }

    @Override
    public void execute() {
        actions.forEach(Action::execute);
    }

    @Override
    public void rollback() {
        for (int i = actions.size() - 1; i >= 0; i--) {
            actions.get(i).rollback();
        }
    }
}
