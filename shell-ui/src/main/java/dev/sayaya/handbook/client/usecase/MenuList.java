package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.SessionState;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 유저 정보 및 세션 상태가 변경되면 메뉴를 다시 필터링하고 로딩한다.
 */
@Singleton
public class MenuList {
    @Delegate private final BehaviorSubject<List<Menu>> _this = behavior(List.of());
    private final MenuRepository menuRepository;
    private final SessionStateProvider sessionStateProvider;
    private final MenuSelected menuSelected;
    private List<Menu> allMenus = new ArrayList<>();

    @Inject MenuList(UserProvider userProvider, 
                    SessionStateProvider sessionStateProvider,
                    MenuRepository menuRepository,
                    MenuSelected menuSelected) {
        this.menuRepository = menuRepository;
        this.sessionStateProvider = sessionStateProvider;
        this.menuSelected = menuSelected;
        userProvider.subscribe(this::update);
        sessionStateProvider.subscribe(state -> filterAndPublish());
    }

    private void update(User user) {
        menuRepository.findAll().subscribe(list -> {
            this.allMenus = list;
            filterAndPublish();
        });
    }

    private void filterAndPublish() {
        SessionState state = sessionStateProvider.getValue();
        if (state == null) return;

        List<Menu> filtered = allMenus.stream()
                .filter(menu -> menu.isAllowedFor(state.kind()))
                .collect(Collectors.toList());

        updateIfChanged(filtered);
        autoSelectIfUnique(filtered);
    }

    private void updateIfChanged(List<Menu> list) {
        List<Menu> current = _this.getValue();
        if (current.size() != list.size() || !new HashSet<>(current).containsAll(list)) {
            next(list);
        }
    }

    private void autoSelectIfUnique(List<Menu> list) {
        // 현재 선택된 메뉴가 없고, 노출 가능한 네비게이션 메뉴(appBarSlot == null)가 단 하나라면 자동 선택
        if (menuSelected.getValue() == null) {
            List<Menu> navMenus = list.stream()
                    .filter(m -> m.appBarSlot() == null)
                    .collect(Collectors.toList());
            if (navMenus.size() == 1) {
                menuSelected.next(navMenus.get(0));
            }
        }
    }
}
