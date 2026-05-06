package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.TypeToolManager;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 선택된 타입을 삭제하는 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link dev.sayaya.handbook.client.components.ConfirmDialog}로 삭제 확인을 요청한 뒤,
 * 확인 시 {@link dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement}에서 선택된 타입 키를 조회하고,
 * 각 타입에 대해 {@link dev.sayaya.handbook.client.usecase.action.DeleteBoxAction}을 실행하여 삭제 마킹한다.</p>
 */
@Singleton
public class RemoveTypeButton implements IsElement<HTMLElement> {
    @Delegate private final IconButtonElementBuilder.OutlinedIconButtonElementBuilder _this;

    @Inject
    RemoveTypeButton(TypeToolManager toolManager, LabelProvider labelProvider) {
        _this = new IconButtonElementBuilder.OutlinedIconButtonElementBuilder()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-trash"))
                .css("type-ctrl-btn", "type-ctrl-btn-remove");

        _this.onClick(e -> toolManager.executeRemove());

        labelProvider.subscribe(labels -> {
            _this.element().title = labels.getOrDefault("type.remove", "Remove");
        });
    }
}
