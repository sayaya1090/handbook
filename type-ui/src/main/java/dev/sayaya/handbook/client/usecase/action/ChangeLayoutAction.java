package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.TypeLayout;

/**
 * 레이아웃 기간을 전환하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 LayoutProvider에 새 레이아웃(after)을 설정하고,
 * rollback 시 이전 레이아웃(before)으로 복원한다.</p>
 */
public class ChangeLayoutAction implements Action {
    private final LayoutProvider layoutProvider;
    private final TypeLayout before;
    private final TypeLayout after;

    public ChangeLayoutAction(LayoutProvider layoutProvider, TypeLayout before, TypeLayout after) {
        this.layoutProvider = layoutProvider;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() {
        layoutProvider.replace(after);
    }

    @Override
    public void rollback() {
        layoutProvider.replace(before);
    }
}
