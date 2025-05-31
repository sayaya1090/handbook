package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;

public interface UpdatableType {
    void update();
    Type value();
}
