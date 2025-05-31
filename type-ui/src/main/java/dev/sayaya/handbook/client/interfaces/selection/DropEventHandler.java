package dev.sayaya.handbook.client.interfaces.selection;

import dev.sayaya.handbook.client.usecase.UpdatableType;

public interface DropEventHandler {
    void onInvoke(int dx, int dy, UpdatableType... box);
}
