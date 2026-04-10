package dev.sayaya.handbook.client.interfaces.selection;

import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 현재 선택된 타입 박스 key 집합. 멀티셀렉트(Ctrl+Click) 지원. */
@Singleton
public class SelectedBoxElement {
    private final BehaviorSubject<Set<String>> subject = behavior(Collections.emptySet());

    @Inject SelectedBoxElement() {}

    public Set<String> getValue() { return subject.getValue(); }

    public void select(String typeKey) {
        subject.next(Set.of(typeKey));
    }

    public void toggle(String typeKey) {
        Set<String> next = new LinkedHashSet<>(subject.getValue());
        if (next.contains(typeKey)) next.remove(typeKey);
        else next.add(typeKey);
        subject.next(next);
    }

    public void clear() {
        subject.next(Collections.emptySet());
    }

    public boolean isSelected(String typeKey) {
        return subject.getValue().contains(typeKey);
    }

    public void subscribe(Consumer<Set<String>> consumer) {
        subject.subscribe(consumer::accept);
    }
}
