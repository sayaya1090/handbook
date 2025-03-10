package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;

public class ComplexAction implements Action {
    private final Action[] actions;
    public ComplexAction(Action... actions) {
        this.actions = actions;
    }
    @Override
    public void execute() {
        for(Action action:actions) action.execute();
    }

    @Override
    public void rollback() {
        for(Action action:actions) action.rollback();
    }
}
