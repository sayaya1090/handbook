package dev.sayaya.handbook.client.interfaces.selection;

import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.client.usecase.PositionMap;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 드래그 중 선택된 박스의 고스트(점선 테두리)를 표시한다.
 * 드래그 시작 시 show(), 이동 시 move(), 완료 시 hide() 호출.
 */
@Singleton
public class DragShapeElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final PositionMap positionMap;
    private final SelectedBoxElement selection;
    private final Map<String, HTMLDivElement> ghosts = new LinkedHashMap<>();
    private Consumer<int[]> dropHandler;

    private int dragOriginX, dragOriginY;
    private int lastMouseX, lastMouseY;
    private boolean active = false;

    private final GridSnap gridSnap;

    @Inject
    DragShapeElement(PositionMap positionMap, SelectedBoxElement selection, GridSnap gridSnap) {
        this.positionMap = positionMap;
        this.selection = selection;
        this.gridSnap = gridSnap;
        root = (HTMLDivElement) DomGlobal.document.createElement("div");
        root.classList.add("drag-ghost-container");
    }

    public void show(int mouseX, int mouseY) {
        dragOriginX = mouseX;
        dragOriginY = mouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        active = true;

        ghosts.values().forEach(g -> g.remove());
        ghosts.clear();
        Set<String> selected = selection.getValue();
        for (String key : selected) {
            Position pos = positionMap.get(key);
            if (pos == null) continue;
            HTMLDivElement ghost = (HTMLDivElement) DomGlobal.document.createElement("div");
            ghost.classList.add("drag-ghost");
            setPosition(ghost, pos.x, pos.y, pos.width, pos.height);
            ghosts.put(key, ghost);
            root.appendChild(ghost);
        }
        root.style.setProperty("display", "block");
    }

    public void move(int mouseX, int mouseY) {
        if (!active) return;
        int dx = mouseX - lastMouseX;
        int dy = mouseY - lastMouseY;
        if (dx == 0 && dy == 0) return;

        for (Map.Entry<String, HTMLDivElement> entry : ghosts.entrySet()) {
            Position pos = positionMap.get(entry.getKey());
            if (pos == null) continue;
            int ghostX = gridSnap.snap(pos.x + (mouseX - dragOriginX));
            int ghostY = gridSnap.snap(pos.y + (mouseY - dragOriginY));
            setPosition(entry.getValue(), ghostX, ghostY, pos.width, pos.height);
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public void drop(int mouseX, int mouseY) {
        if (!active) return;
        active = false;
        root.style.setProperty("display", "none");
        ghosts.values().forEach(g -> g.remove());
        ghosts.clear();

        int totalDx = mouseX - dragOriginX;
        int totalDy = mouseY - dragOriginY;
        if ((totalDx != 0 || totalDy != 0) && dropHandler != null) {
            dropHandler.accept(new int[]{totalDx, totalDy});
        }
    }

    public void cancel() {
        active = false;
        root.style.setProperty("display", "none");
        ghosts.values().forEach(g -> g.remove());
        ghosts.clear();
    }

    public boolean isActive() { return active; }

    public void onDrop(Consumer<int[]> handler) {
        this.dropHandler = handler;
    }

    /**
     * Elemento의 style.set()은 GWT에서 동작하지 않으므로 setProperty()를 사용한다.
     */
    private static void setPosition(HTMLDivElement el, int x, int y, int w, int h) {
        el.style.setProperty("left", x + "px");
        el.style.setProperty("top", y + "px");
        el.style.setProperty("width", w + "px");
        el.style.setProperty("height", h + "px");
    }

    @Override
    public HTMLDivElement element() { return root; }
}
