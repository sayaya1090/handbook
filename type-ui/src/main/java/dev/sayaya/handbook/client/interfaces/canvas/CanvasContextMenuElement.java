package dev.sayaya.handbook.client.interfaces.canvas;


import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeSearchProvider;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.CreateBoxAction;
import dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.sayaya.handbook.client.interfaces.ContextMenuHelper.menuItem;
import static dev.sayaya.handbook.client.interfaces.ContextMenuHelper.uniqueTypeId;
import static org.jboss.elemento.Elements.div;

/** 캔버스 빈 영역 우클릭 시 표시되는 컨텍스트 메뉴. */
@Singleton
public class CanvasContextMenuElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final ActionManager actionManager;
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final LayoutProvider layoutProvider;
    private final TypeSearchProvider typeSearchProvider;
    private int clickX, clickY;

    @Inject
    CanvasContextMenuElement(ActionManager actionManager, TypeList typeList, PositionMap positionMap,
                             ChangeTracker tracker, LayoutProvider layoutProvider, 
                             TypeSearchProvider typeSearchProvider, LabelProvider labelProvider) {
        this.actionManager = actionManager;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.layoutProvider = layoutProvider;
        this.typeSearchProvider = typeSearchProvider;

        HTMLElement addItem = menuItem("Add Type");
        HTMLElement undoItem = menuItem("Undo");
        HTMLElement redoItem = menuItem("Redo");
        HTMLElement reloadItem = menuItem("Reload");

        addItem.addEventListener("click", e -> { hide(); addTypeAt(clickX, clickY); });
        undoItem.addEventListener("click", e -> { hide(); actionManager.undo(); });
        redoItem.addEventListener("click", e -> { hide(); actionManager.redo(); });
        reloadItem.addEventListener("click", e -> hide());

        actionManager.onCanUndo(can -> undoItem.classList.toggle("ctx-disabled", !can));
        actionManager.onCanRedo(can -> redoItem.classList.toggle("ctx-disabled", !can));

        labelProvider.subscribe(labels -> {
            addItem.textContent = labels.getOrDefault("type.add", "Add Type");
            undoItem.textContent = labels.getOrDefault("type.undo", "Undo");
            redoItem.textContent = labels.getOrDefault("type.redo", "Redo");
            reloadItem.textContent = labels.getOrDefault("type.reload", "Reload");
        });

        root = div().css("ctx-menu", "ctx-canvas-menu")
                .add(addItem)
                .add(div().css("ctx-divider"))
                .add(undoItem)
                .add(redoItem)
                .add(div().css("ctx-divider"))
                .add(reloadItem)
                .element();
        root.style.setProperty("display", "none");

        DomGlobal.document.addEventListener("click", e -> hide());
    }

    public void show(int x, int y) {
        elemental2.dom.DomGlobal.console.log("[CanvasContextMenu] show() at " + x + ", " + y);
        clickX = x;
        clickY = y;
        root.style.setProperty("display", "flex");
        root.style.setProperty("left", x + "px");
        root.style.setProperty("top", y + "px");
        root.style.setProperty("z-index", "10005");
    }

    public void hide() {
        elemental2.dom.DomGlobal.console.log("[CanvasContextMenu] hide()");
        root.style.setProperty("display", "none");
    }

    private void addTypeAt(int x, int y) {
        TypeLayout layout = layoutProvider.getValue();
        if (layout == null) {
            throw new IllegalStateException("Cannot add type: No active layout period.");
        }
        
        Set<String> activeKeys = typeSearchProvider.getVisibleTypes().stream()
                .map(Type::key).collect(Collectors.toSet());
        
        String id = uniqueTypeId(typeList);
        Type newType = Type.create(id, "1.0", layout.effectDateTime(), layout.expireDateTime());
        Position pos = Position.of(x, y, 240, 160);
        actionManager.execute(new ComplexAction(
                new CreateBoxAction(typeList, positionMap, tracker, newType, pos),
                new PushOutOverlapAction(positionMap, newType.key(), 10, activeKeys)
        ));
    }


    @Override
    public HTMLDivElement element() { return root; }
}
