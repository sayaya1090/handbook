package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.domain.Action;

import java.util.Arrays;
import java.util.List;

/** 여러 액션을 하나로 묶는 복합 액션. undo 시 역순으로 롤백. */
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
