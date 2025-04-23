package dev.sayaya.handbook.client.interfaces.create;

import dagger.assisted.AssistedFactory;
import dev.sayaya.handbook.client.usecase.create.CreateWorkspaceState;

@AssistedFactory
public interface SelectionElementFactory {
    SectionElement create(CreateWorkspaceState state);
}
