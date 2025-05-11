package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;

public interface UpdatableBoxList {
    UpdatableBox[] values();
    int estimateBoxHeight(Type box);
}
