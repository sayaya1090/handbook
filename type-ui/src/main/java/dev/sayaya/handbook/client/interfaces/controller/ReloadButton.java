package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

/**
 * 서버에서 타입과 레이아웃을 다시 로드하는 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link LoadAction}을 생성하여 실행한다.
 * 레이아웃 목록 → 기간 자동 선택 → 타입 + 위치 로드를 수행한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeRepository} — 타입 조회 API</li>
 *   <li>{@link LayoutRepository} — 레이아웃 조회 API</li>
 *   <li>{@link TypeList}, {@link PositionMap} — 상태 교체 대상</li>
 *   <li>{@link ChangeTracker}, {@link ActionManager} — 로드 후 초기화</li>
 *   <li>{@link LayoutProvider}, {@link LayoutList} — 기간 선택</li>
 *   <li>{@link LabelProvider} — 다국어 툴팁</li>
 * </ul></p>
 * <p><b>주의:</b> LoadAction은 되돌릴 수 없다(스택 초기화).</p>
 */
@Singleton
public class ReloadButton implements IsElement<HTMLElement> {
    @Delegate private final IconButtonElementBuilder.PlainIconButtonElementBuilder _this;

    @Inject
    ReloadButton(TypeRepository typeRepository, LayoutRepository layoutRepository,
                 TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                 ActionManager actionManager, LayoutProvider layoutProvider, LayoutList layoutList,
                 LabelProvider labelProvider) {
        _this = button().icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-rotate"))
                .css("type-ctrl-btn", "type-ctrl-btn-reload");

        _this.onClick(e ->
                new LoadAction(typeRepository, layoutRepository, typeList, positionMap,
                        tracker, actionManager, layoutProvider, layoutList).execute()
        );

        labelProvider.subscribe(labels ->
                _this.element().title = labels.getOrDefault("type.reload", "Reload"));
    }
}
