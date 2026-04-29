package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.RenderSharing;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 애플리케이션의 현재 렌더링 상태를 관리하는 저장소.
 * 
 * <p><b>역할:</b> 셸의 중앙 영역에 표시될 콘텐츠 명세(IFrame 또는 커스텀 렌더)를 소유한다.
 * FrameUpdater 가 이 상태를 구독하여 실제 DOM 프레임을 생성하거나 갱신한다.</p>
 */
@Singleton
public class RenderStore extends BehaviorSubject<Render> {
    @Inject
    public RenderStore() {
        super(null);
        RenderSharing.register(value -> this.next((Render)Js.cast(value)));
    }
}

