package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.AssistedFactory;
import dev.sayaya.handbook.client.domain.Menu;

@AssistedFactory
public interface MenuRailItemFactory {
    MenuRailItemElement item(Menu menu);
}
