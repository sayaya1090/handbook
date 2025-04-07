package dev.sayaya.handbook.client.interfaces.drawer;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.ui.elements.IconElementBuilder;

public class ToolRailItemElement extends NavigationRailItemElement {
    @AssistedInject ToolRailItemElement(@Assisted Tool tool) {
        icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon))
                .start(IconElementBuilder.icon().css("fa-sharp", "fa-light", tool.icon))
                .headline(tool.title);
    }
}
