package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 전체 레이아웃 기간 목록. 서버에서 조회하여 갱신한다. */
@Singleton
public class LayoutList {
    @Delegate private final BehaviorSubject<List<LayoutPeriod>> _this = behavior(Collections.emptyList());
    @Inject LayoutList() {}
}
