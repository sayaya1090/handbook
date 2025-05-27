package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.rx.subject.ReplaySubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import static dev.sayaya.rx.subject.ReplaySubject.replayWithBuffer;

@Singleton
public class UserProvider {
    @Delegate private final ReplaySubject<User> _this = replayWithBuffer(User.class, 1);
    @Inject UserProvider(UserRepository repo) {
        repo.find().subscribe(_this);
    }
}
