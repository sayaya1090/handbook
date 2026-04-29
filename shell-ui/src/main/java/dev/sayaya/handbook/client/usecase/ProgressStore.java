package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.ProgressSharing;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 애플리케이션의 전역 진행 상태(Progress)를 관리하는 저장소.
 * 
 * <p><b>역할:</b> 셸 상단의 프로그레스 바에 표시될 백그라운드 작업의 진행률을 소유한다.
 * GWT 모듈 간 브릿지로부터 수신된 상태를 UI 요소로 전달하는 중간 매개체 역할을 한다.</p>
 */
@Singleton
public class ProgressStore extends BehaviorSubject<Progress> {
    @Inject
    public ProgressStore() {
        super(null);
        ProgressSharing.register(value -> this.next((Progress)Js.cast(value)));
    }
}

