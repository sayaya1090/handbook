package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

@Singleton
public class SnapCheckbox implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    SnapCheckbox(GridSnap gridSnap, LabelProvider labelProvider) {
        HTMLInputElement checkbox = (HTMLInputElement) DomGlobal.document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.id = "snap-toggle";
        checkbox.checked = gridSnap.isEnabled();
        checkbox.addEventListener("change", e -> gridSnap.setEnabled(checkbox.checked));

        HTMLElement label = (HTMLElement) DomGlobal.document.createElement("label");
        label.setAttribute("for", "snap-toggle");
        label.textContent = "Snap";
        label.style.setProperty("font-size", "13px");
        label.style.setProperty("cursor", "pointer");
        label.style.setProperty("user-select", "none");

        labelProvider.subscribe(labels ->
                label.textContent = labels.getOrDefault("type.snap", "Snap"));

        root = div().css("type-ctrl-group").element();
        root.appendChild(checkbox);
        root.appendChild(label);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
