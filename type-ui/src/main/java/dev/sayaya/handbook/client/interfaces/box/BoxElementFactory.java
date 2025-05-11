package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.AssistedFactory;
import dev.sayaya.handbook.client.domain.Type;

@AssistedFactory
interface BoxElementFactory {
    BoxElement create(Type box);
}
