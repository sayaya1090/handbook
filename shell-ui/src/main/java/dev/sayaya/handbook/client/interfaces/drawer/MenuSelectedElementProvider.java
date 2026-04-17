package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 현재 선택된 {@link MenuRailItemElement} 를 제공한다.
 *
 * <p><b>책임:</b> ToolRail 이 메뉴 아이템 옆에 수직 정렬될 때 기준이 되는
 * 아이템 요소를 공유한다. 과거 MenuHoverElementProvider 였으나 UC-S6 (hover peek)
 * 폐기로 click 기반으로 전환, 이름도 "Selected" 로 정정.</p>
 *
 * <p><b>공급자:</b> {@link MenuRailItemElement#initEventHandlers} 의 click
 * 리스너가 자신을 {@code next(this)} 로 발행한다.</p>
 *
 * <p><b>소비자:</b> {@link ToolRailElement#offset} 이 {@code getValue()} 로 기준
 * 요소를 받아 수직 정렬.</p>
 */
@Singleton
public class MenuSelectedElementProvider {
    @Delegate private final BehaviorSubject<MenuRailItemElement> _this = behavior(null);
    @Inject MenuSelectedElementProvider() {}
}
