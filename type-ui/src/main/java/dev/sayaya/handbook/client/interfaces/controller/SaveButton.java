package dev.sayaya.handbook.client.interfaces.controller;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.SaveAction;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.client.interfaces.api.LabelProvider;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 변경 사항을 서버에 저장하는 Filled 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link SaveAction}을 생성하여 실행한다.
 * 변경/삭제된 타입과 레이아웃 위치를 서버에 전송하고 성공 토스트를 표시한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeRepository} — 타입 저장/삭제 API</li>
 *   <li>{@link LayoutRepository} — 레이아웃 위치 저장 API</li>
 *   <li>{@link TypeList}, {@link PositionMap} — 현재 상태 조회</li>
 *   <li>{@link ChangeTracker} — 변경/삭제 키 조회</li>
 *   <li>{@link ActionManager} — 저장 후 스택 초기화</li>
 *   <li>{@link ToastContainer} — 성공 피드백 토스트 표시</li>
 *   <li>{@link LabelProvider} — 다국어 레이블</li>
 * </ul></p>
 * <p><b>주의:</b> SaveAction은 되돌릴 수 없다(rollback이 no-op).</p>
 */
@Singleton
public class SaveButton implements IsElement<HTMLElement> {
    @Delegate private final ButtonElementBuilder.FilledButtonElementBuilder _this;
    private Labels currentLabels = Labels.empty();

    @Inject
    SaveButton(TypeRepository typeRepository, LayoutRepository layoutRepository,
               TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
               ActionManager actionManager, LayoutProvider layoutProvider,
               ToastContainer toastContainer, LabelProvider labelProvider) {
        _this = ButtonElementBuilder.button().filled()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-floppy-disk"))
                .css("type-ctrl-btn", "type-ctrl-btn-save");

        _this.onClick(e ->
                new SaveAction(typeRepository, layoutRepository, typeList, positionMap,
                        tracker, actionManager, layoutProvider, toastContainer, currentLabels).execute()
        );

        labelProvider.subscribe(labels -> {
            currentLabels = labels;
            _this.text(labels.getOrDefault("type.save", "Save"));
        });
    }
}
