package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

/** 
 * 서버에서 데이터를 다시 로드하는 버튼. 
 */
@Singleton
public class ReloadButton implements IsElement<HTMLElement> {
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder _this;

    private dev.sayaya.handbook.domain.Labels currentLabels = dev.sayaya.handbook.domain.Labels.empty();

    @Inject
    ReloadButton(TypeRepository typeRepository, LayoutRepository layoutRepository,
                 TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                 ActionManager actionManager, LayoutProvider layoutProvider, LayoutList layoutList,
                 dev.sayaya.handbook.client.usecase.TypeDataCoordinator typeDataCoordinator,
                 LabelProvider labelProvider, ConfirmDialog confirmDialog) {
        _this = button().icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-rotate-right"))
                .css("type-ctrl-btn", "type-ctrl-btn-reload");

        _this.onClick(e -> {
            Runnable reloadAction = () -> new LoadAction(typeRepository, layoutRepository, typeList, positionMap,
                    tracker, actionManager, layoutProvider, layoutList,
                    typeDataCoordinator).execute();

            if (tracker.hasChanges()) {
                String message = currentLabels.getOrDefault("type.reload.confirm", "Unsaved changes exist. Are you sure you want to discard them and reload?");
                confirmDialog.show(message, new String[]{"Yes", "No"}, option -> {
                    if ("Yes".equals(option)) reloadAction.run();
                });
            } else {
                reloadAction.run();
            }
        });

        labelProvider.subscribe(labels -> {
            this.currentLabels = labels;
            _this.element().title = labels.getOrDefault("type.reload", "Reload");
        });
    }

    @Override
    public HTMLElement element() { return _this.element(); }
}
