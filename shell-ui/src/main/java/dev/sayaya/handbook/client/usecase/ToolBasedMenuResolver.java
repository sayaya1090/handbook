package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.domain.Tool;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/*
 * 툴이 선택되면 Parent에 해당하는 Menu를 선택 상태로 업데이트한다.
 */
@Singleton
public class ToolBasedMenuResolver {
    private final Map<Tool, Menu> map = new HashMap<>();
    private final MenuList menu;
    private final ToolSelected toolSelected;
    private final MenuSelected menuSelect;

    @Inject ToolBasedMenuResolver(MenuList menu, ToolSelected tool, MenuSelected menuSelect) {
        this.menu = menu;
        this.toolSelected = tool;
        this.menuSelect = menuSelect;
    }
    public void initialize() {
        menu.subscribe(this::update);
        toolSelected.subscribe(this::resolve);
    }
    private void update(List<Menu> menu) {
        map.clear();
        menu.stream()
            .filter(Objects::nonNull)
            .filter(m -> m.tools() != null)
            .forEach(m -> Arrays.stream(m.tools())
                .filter(Objects::nonNull)
                .forEach(t -> map.put(t, m))
            );
    }
    private void resolve(Tool tool) {
        if(tool == null) return;
        if(map.containsKey(tool)) menuSelect.next(map.get(tool));
    }
}
