package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.api.LayoutApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
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

    @Singleton
    public static class EditorContext {
        public final ActionManager actionManager;
        public final ChangeTracker tracker;
        public final TypeApi typeRepository;
        public final LayoutApi layoutRepository;
        public final dev.sayaya.handbook.client.usecase.TypeList typeList;
        public final dev.sayaya.handbook.client.usecase.PositionMap positionMap;
        public final LayoutProvider layoutProvider;
        public final LayoutList layoutList;
        public final dev.sayaya.handbook.client.components.ToastContainer toastContainer;
        public final LabelProvider labelProvider;

        @Inject
        public EditorContext(ActionManager actionManager, ChangeTracker tracker, 
                             TypeApi typeRepository, LayoutApi layoutRepository,
                             dev.sayaya.handbook.client.usecase.TypeList typeList,
                             dev.sayaya.handbook.client.usecase.PositionMap positionMap,
                             LayoutProvider layoutProvider, LayoutList layoutList,
                             dev.sayaya.handbook.client.components.ToastContainer toastContainer,
                             LabelProvider labelProvider) {
            this.actionManager = actionManager;
            this.tracker = tracker;
            this.typeRepository = typeRepository;
            this.layoutRepository = layoutRepository;
            this.typeList = typeList;
            this.positionMap = positionMap;
            this.layoutProvider = layoutProvider;
            this.layoutList = layoutList;
            this.toastContainer = toastContainer;
            this.labelProvider = labelProvider;
        }
    }

    @Inject
    StatusHeaderElement(EditorContext context) {
        
        var undoBtn = new UndoButton(context.actionManager, context.labelProvider);
        var redoBtn = new RedoButton(context.actionManager, context.labelProvider);
        var saveBtn = new SaveButton(context.typeRepository, context.layoutRepository, context.typeList, context.positionMap, context.tracker, context.actionManager, context.layoutProvider, context.toastContainer, context.labelProvider);
        var reloadBtn = new ReloadButton(context.typeRepository, context.layoutRepository, context.typeList, context.positionMap, context.tracker, context.actionManager, context.layoutProvider, context.layoutList, context.labelProvider);

        _this.add(div().css("type-ctrl-group").add(undoBtn).add(redoBtn))
             .add(div().css("type-ctrl-group").add(saveBtn).add(reloadBtn));
    }

    @Override
    public HTMLDivElement element() { return _this.element(); }
}
