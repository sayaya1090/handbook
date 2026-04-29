package dev.sayaya.handbook.client.onboarding;

import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface SectionElementFactory {
    SectionElement create(String modeName);
}
