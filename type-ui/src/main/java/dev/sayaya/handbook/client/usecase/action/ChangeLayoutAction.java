package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.usecase.LayoutProvider;

/** 레이아웃 기간 전환. Undo 시 이전 기간으로 복원. */
public class ChangeLayoutAction implements Action {
    private final LayoutProvider layoutProvider;
    private final LayoutPeriod before;
    private final LayoutPeriod after;

    public ChangeLayoutAction(LayoutProvider layoutProvider, LayoutPeriod before, LayoutPeriod after) {
        this.layoutProvider = layoutProvider;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() {
        layoutProvider.next(after);
    }

    @Override
    public void rollback() {
        layoutProvider.next(before);
    }
}
