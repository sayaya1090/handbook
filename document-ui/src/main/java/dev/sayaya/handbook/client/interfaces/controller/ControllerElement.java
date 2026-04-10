package dev.sayaya.handbook.client.interfaces.controller;

import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/** 상단 툴바. 타입 탭, CRUD 버튼, Undo/Redo, 페이지네이션을 포함한다. */
@Singleton
public class ControllerElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;

    @Inject
    public ControllerElement(TypeTabsElement tabs, AddButton addButton, DeleteButton deleteButton,
                              SaveButton saveButton, UndoButton undoButton, RedoButton redoButton) {
        this.element = div().css("doc-controller")
                .add(tabs)
                .add(div().css("doc-ctrl-actions")
                        .add(addButton)
                        .add(deleteButton)
                        .add(undoButton)
                        .add(redoButton)
                        .add(saveButton))
                .element();
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
