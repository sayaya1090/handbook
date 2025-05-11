package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class UserProvider {
    @Delegate private final BehaviorSubject<User> _this = behavior(null);
    @Inject UserProvider(UserRepository repo, DrawerMode drawerMode) {
        drawerMode.subscribe(m->{
            DomGlobal.console.log("drawerMode changed: " + m + " User provider -> " + _this.getValue());
            if(_this.getValue()==null) repo.find().subscribe(_this);
        });
    }
}
