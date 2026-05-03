package dev.sayaya.handbook.client.interfaces.controller;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.ContextMenuHelper;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.CreateBoxAction;
import dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 새 타입을 캔버스에 추가하는 Filled 버튼.
 *
 * <p><b>책임:</b> 클릭 시 현재 레이아웃 기간에 맞는 새 Type를 생성하고,
 * {@link CreateBoxAction} + {@link PushOutOverlapAction}을 {@link ComplexAction}으로 묶어 실행한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ActionManager} — 액션 실행 및 Undo/Redo 스택 관리</li>
 *   <li>{@link TypeList} — 타입 목록 상태</li>
 *   <li>{@link PositionMap} — 위치 상태</li>
 *   <li>{@link ChangeTracker} — 더티 트래킹</li>
 *   <li>{@link LayoutProvider} — 현재 기간 조회</li>
 *   <li>{@link LabelProvider} — 다국어 레이블</li>
 * </ul></p>
 * <p><b>주의:</b> 고유 타입 ID는 {@link ContextMenuHelper#uniqueTypeId(TypeList)}로 생성된다.</p>
 */
@Singleton
public class AddTypeButton implements IsElement<HTMLElement> {
    @Delegate private final ButtonElementBuilder.FilledButtonElementBuilder _this;

    @Inject
    AddTypeButton(ActionManager actionManager, TypeList typeList, PositionMap positionMap,
                  ChangeTracker tracker, LayoutProvider layoutProvider, LabelProvider labelProvider) {
        _this = ButtonElementBuilder.button().filled()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-plus"))
                .css("type-ctrl-btn", "type-ctrl-btn-add");

        _this.onClick(e -> {
            LayoutPeriod period = layoutProvider.getValue();
            if (period == null) return;
            String id = ContextMenuHelper.uniqueTypeId(typeList);
            Type newType = Type.create(id, "1.0", 0.0, 0.0); // Skip setting effectDateTime, expireDateTime
            Position pos = Position.of(50, 80, 240, 160);
            actionManager.execute(new ComplexAction(
                    new CreateBoxAction(typeList, positionMap, tracker, newType, pos),
                    new PushOutOverlapAction(positionMap, newType.key(), 10)
            ));
        });

        labelProvider.subscribe(labels ->
                _this.text(labels.getOrDefault("type.add", "Add")));
    }
}
