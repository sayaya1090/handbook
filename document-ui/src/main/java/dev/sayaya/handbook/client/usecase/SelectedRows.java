package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 스프레드시트에서 체크박스로 선택된 행 인덱스의 반응형 상태 홀더.
 *
 * <p><b>책임:</b> BehaviorSubject로 선택된 행 인덱스 집합을 관리하고,
 * UI 컴포넌트(BulkDeleteButton, BulkStatusButton)가 구독할 수 있는 Observable을 제공한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul><li>{@link BehaviorSubject} — 반응형 상태 관리</li></ul></p>
 */
@Singleton
public class SelectedRows {
    @Delegate private final BehaviorSubject<Set<Integer>> _this = behavior(Collections.emptySet());

    @Inject SelectedRows() {}

    public void toggle(int rowIndex) {
        Set<Integer> current = new LinkedHashSet<>(_this.getValue());
        if (current.contains(rowIndex)) current.remove(rowIndex);
        else current.add(rowIndex);
        _this.next(current);
    }

    public void clear() { _this.next(Collections.emptySet()); }
}
