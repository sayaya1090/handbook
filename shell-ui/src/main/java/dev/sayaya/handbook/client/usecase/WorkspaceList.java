package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class WorkspaceList {
    @Delegate private final BehaviorSubject<List<Workspace>> _this = behavior(List.of());
    @Inject WorkspaceList(UserProvider user) {
        user.subscribe(this::update);
    }
    private void update(User user) {
        if(user.workspaces()==null) next(List.of());
        else next(Arrays.stream(user.workspaces()).collect(Collectors.toList()));
    }
}
