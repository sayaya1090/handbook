package dev.sayaya.handbook.client.interfaces.controller.toolbar;

import dev.sayaya.handbook.client.interfaces.controller.NewVersionButton;
import dev.sayaya.handbook.client.interfaces.controller.RemoveTypeButton;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class TypeFloatingToolbar implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root = div().css("type-floating-toolbar").element();

    @Inject
    TypeFloatingToolbar(SelectedBoxElement selection, RemoveTypeButton removeBtn, NewVersionButton newVersionBtn) {
        root.appendChild(removeBtn.element());
        root.appendChild(newVersionBtn.element());

        selection.subscribe(selected -> {
            boolean hasSelection = selected != null && !selected.isEmpty();
            if (hasSelection) root.classList.add("visible");
            else root.classList.remove("visible");
        });
    }

    @Override
    public HTMLDivElement element() { return root; }
}
