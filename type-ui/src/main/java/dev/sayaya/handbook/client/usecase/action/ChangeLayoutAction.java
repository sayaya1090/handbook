package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.client.usecase.LayoutProvider;

/**
 * 레이아웃 기간을 전환하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 LayoutProvider에 새 기간(after)을 설정하고,
 * rollback 시 이전 기간(before)으로 복원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link LayoutProvider} — 기간 상태 변경</li>
 * </ul></p>
 * <p><b>주의:</b> Before/AfterButton에서 기간 탐색 시 사용된다.
 * 기간 변경 시 타입 목록 재로딩은 LayoutProvider 구독자가 처리한다.</p>
 */
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
        layoutProvider.replace(after);
    }

    @Override
    public void rollback() {
        layoutProvider.replace(before);
    }
}
