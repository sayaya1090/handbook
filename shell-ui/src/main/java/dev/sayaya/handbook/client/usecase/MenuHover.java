package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * MenuRail EXPAND 상태에서만 발행되는 hover 프리뷰 채널.
 *
 * <p><b>책임 범위:</b> {@link ToolList} 가 {@code MenuSelected} 와 merge 하여
 * "선택 + hover 탐색" 양쪽을 도구 목록에 반영한다. {@code MenuRailMode == EXPAND}
 * 일 때만 {@link dev.sayaya.handbook.client.interfaces.drawer.MenuRailItemElement}
 * 가 발행하고, COLLAPSE 에서는 대신 {@code TooltipCard} 가 라벨만 표시한다.</p>
 *
 * <p><b>이력:</b> 과거 UC-S6 는 MenuRail 상태와 무관하게 hover 시 전환되어
 * CloseToolRailButton 복귀 의사를 무시하는 문제가 있었음. 2026-04-17 부터
 * EXPAND 조건부로 제한하여 "탐색 중에는 peek, 선택 후에는 정착" 패턴을 복원.</p>
 */
@Singleton
public class MenuHover {
    @Delegate private final BehaviorSubject<Menu> _this = behavior(null);
    @Inject MenuHover() {}
}
