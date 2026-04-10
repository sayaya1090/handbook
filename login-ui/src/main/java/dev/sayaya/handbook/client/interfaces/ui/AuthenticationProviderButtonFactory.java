package dev.sayaya.handbook.client.interfaces.ui;

import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface AuthenticationProviderButtonFactory {
    AuthenticationProviderButton button(String provider);
}
