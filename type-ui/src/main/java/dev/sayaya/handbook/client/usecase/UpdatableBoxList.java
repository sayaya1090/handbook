package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Box;

public interface UpdatableBoxList {
    UpdatableBox[] values();
    int estimateBoxHeight(Box box);
}
