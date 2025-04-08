package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/*
 * 드로어가 열리면 메뉴를 다시 로딩한다.
 * 툴이 선택되면 Parent에 해당하는 Menu를 선택 상태로 업데이트한다.
 * -> Tool이 Menu에 대한 참조 정보가 없어 모든 Menu를 탐색해야 함. 향후 개선 필요
 */
@Singleton
public class MenuList {
    @Delegate private final BehaviorSubject<List<Menu>> _this = behavior(List.of());
    private final MenuRepository menuRepository;
    @Inject MenuList(DrawerMode drawerMode, MenuRepository menuRepository, MenuSelected menuSelected, ToolSelected toolSelected) {
        this.menuRepository = menuRepository;
        drawerMode.subscribe(this::update);
        toolSelected.subscribe(tool->selectMenuIfToolChanged(tool, menuSelected));
    }
    private void update(DrawerState state) {
        if(state != DrawerState.EXPAND) return;
        menuRepository.findAll().subscribe(this::updateIfChanged);
    }
    private void updateIfChanged(List<Menu> list) {
        var current = new HashSet<>(_this.getValue());
        if(current.size() != list.size() || !current.containsAll(list)) next(list);
    }
    private void selectMenuIfToolChanged(Tool tool, MenuSelected menuSelected) {
        _this.getValue().stream()
                .filter(m -> m.tools != null)
                .filter(m -> Arrays.stream(m.tools).anyMatch(t -> t == tool))
                .findFirst()
                .ifPresent(menuSelected::next);
    }
}
