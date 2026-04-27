package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.api.LayoutApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.client.usecase.action.SaveAction;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class StatusHeaderElement implements IsElement<HTMLDivElement> {
    private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("type-status-header");

    @Inject
    StatusHeaderElement(ActionManager actionManager, ChangeTracker tracker, 
                        TypeApi typeRepository, LayoutApi layoutRepository,
                        dev.sayaya.handbook.client.usecase.TypeList typeList,
                        dev.sayaya.handbook.client.usecase.PositionMap positionMap,
                        LayoutProvider layoutProvider, LayoutList layoutList,
                        dev.sayaya.handbook.client.components.ToastContainer toastContainer,
                        LabelProvider labelProvider) {
        
        var undoBtn = new UndoButton(actionManager, labelProvider);
        var redoBtn = new RedoButton(actionManager, labelProvider);
        var saveBtn = new SaveButton(typeRepository, layoutRepository, typeList, positionMap, tracker, actionManager, layoutProvider, toastContainer, labelProvider);
        var reloadBtn = new ReloadButton(typeRepository, layoutRepository, typeList, positionMap, tracker, actionManager, layoutProvider, layoutList, labelProvider);

        _this.add(div().css("type-ctrl-group").add(undoBtn).add(redoBtn))
             .add(div().css("type-ctrl-group").add(saveBtn).add(reloadBtn));
    }

    @Override
    public HTMLDivElement element() { return _this.element(); }
}
