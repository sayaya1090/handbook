package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class UserProvider {
    @Delegate private final BehaviorSubject<User> _this = behavior(null);
    private final UserRepository repo;
    private final DrawerMode drawerMode;
    @Inject UserProvider(UserRepository repo, DrawerMode drawerMode) {
        this.repo = repo;
        this.drawerMode = drawerMode;
    }
    public void initialize() {
        drawerMode.subscribe(m->{
            if(_this.getValue()==null) repo.find().subscribe(_this);
        });
    }
}
