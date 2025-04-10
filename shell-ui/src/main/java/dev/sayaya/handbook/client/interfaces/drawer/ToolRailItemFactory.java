package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.AssistedFactory;
import dev.sayaya.handbook.client.domain.Tool;

@AssistedFactory
public interface ToolRailItemFactory {
    ToolRailItemElement item(Tool tool);
}
