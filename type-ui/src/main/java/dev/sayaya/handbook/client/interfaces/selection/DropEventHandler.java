package dev.sayaya.handbook.client.interfaces.selection;

import dev.sayaya.handbook.client.usecase.UpdatableBox;

public interface DropEventHandler {
    void onInvoke(int dx, int dy, UpdatableBox... box);
}
