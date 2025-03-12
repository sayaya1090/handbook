package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.UpdatableBox;

public interface DropEventHandler {
    void onInvoke(UpdatableBox box, int dx, int dy);
}
