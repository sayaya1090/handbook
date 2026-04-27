package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.User;
import dev.sayaya.rx.Observable;

public interface UserRepository {
    Observable<User> find();
}
