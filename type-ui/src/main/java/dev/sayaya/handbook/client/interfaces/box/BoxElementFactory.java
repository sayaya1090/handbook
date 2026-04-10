package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.AssistedFactory;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.domain.TypeValue;

@AssistedFactory
public interface BoxElementFactory {
    TypeElement create(TypeValue type, Position position);
}
