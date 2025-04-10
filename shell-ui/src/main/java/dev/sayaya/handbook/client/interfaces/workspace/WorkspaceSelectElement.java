package dev.sayaya.handbook.client.interfaces.workspace;

import dev.sayaya.handbook.client.usecase.WorkspaceProvider;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class WorkspaceSelectElement implements IsElement<HTMLElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div();
    @Inject WorkspaceSelectElement(WorkspaceProvider provider) {
    }
}
