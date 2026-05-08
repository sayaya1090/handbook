package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.interfaces.editor.VersionCreationDialog;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Type;
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
 * 선택된 타입의 새로운 버전을 생성하는 버튼.
 *
 * <p><b>책임:</b> 타입을 1개 선택했을 때 활성화되며, 클릭 시 {@link VersionCreationDialog}를 호출한다.</p>
 */
@Singleton
public class NewVersionButton implements IsElement<HTMLElement> {
    @Delegate private final IconButtonElementBuilder.PlainIconButtonElementBuilder _this;
    private Type currentType;

    @Inject
    NewVersionButton(SelectedBoxElement selection, TypeList typeList, LabelProvider labelProvider,
                     VersionCreationDialog creationDialog) {
        _this = button().icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-code-branch"))
                .css("type-ctrl-btn", "type-ctrl-btn-new-version")
                .disabled(true);

        selection.subscribe(selected -> {
            if (selected.size() == 1) {
                String key = selected.iterator().next();
                currentType = typeList.getValue().stream().filter(t -> t.key().equals(key)).findFirst().orElse(null);
                _this.disabled(currentType == null);
            } else {
                _this.disabled(true);
                currentType = null;
            }
        });

        labelProvider.subscribe(labels ->
                _this.element().title = labels.getOrDefault("type.new_version", "Create New Version"));
        
        _this.onClick(e -> {
            e.stopPropagation();
            if (currentType != null) creationDialog.show(currentType);
        });
    }
}
