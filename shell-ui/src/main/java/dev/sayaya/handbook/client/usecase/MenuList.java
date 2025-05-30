package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/*
 * 유저 정보가 변경되면 메뉴를 다시 로딩한다.
 */
@Singleton
public class MenuList {
    @Delegate private final BehaviorSubject<List<Menu>> _this = behavior(List.of());
    private final MenuRepository menuRepository;
    @Inject MenuList(UserProvider userProvider, MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
        userProvider.subscribe(this::update);
    }
    private void update(User user) {
        menuRepository.findAll().subscribe(this::updateIfChanged);
    }
    private void updateIfChanged(List<Menu> list) {
        var current = new HashSet<>(_this.getValue());
        if(current.size() != list.size() || !current.containsAll(list)) next(list);
    }
}
