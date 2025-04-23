package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.rx.Observable;

public interface UserRepository {
    Observable<User> find();
}
