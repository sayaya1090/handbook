package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.AssistedFactory;
import dev.sayaya.handbook.client.domain.Box;

@AssistedFactory
interface BoxElementFactory {
    BoxElement create(Box box);
}
