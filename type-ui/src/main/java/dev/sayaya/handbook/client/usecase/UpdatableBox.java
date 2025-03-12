package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Box;

public interface UpdatableBox {
    void update();
    Box box();
}
