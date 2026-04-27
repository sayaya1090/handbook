package dev.sayaya.handbook.client.onboarding;

import dagger.assisted.AssistedFactory;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode.Mode;

@AssistedFactory
public interface SectionElementFactory {
    SectionElement create(Mode mode);
}
