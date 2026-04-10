package dev.sayaya.handbook.client.interfaces.controller;

import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 캔버스 상단 툴바. 기간 이동, 타입 CRUD, Undo/Redo, 저장 버튼.
 */
@Singleton
public class ControllerElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    ControllerElement(BeforeButton beforeBtn, AfterButton afterBtn,
                      AddTypeButton addBtn, RemoveTypeButton removeBtn,
                      UndoButton undoBtn, RedoButton redoBtn,
                      SaveButton saveBtn, ReloadButton reloadBtn,
                      SnapCheckbox snapCheckbox, ModeToggleButton modeToggle) {
        root = div().css("type-controller")
                .add(modeToggle)
                .add(div().css("type-ctrl-group")
                        .add(beforeBtn).add(afterBtn))
                .add(div().css("type-ctrl-group")
                        .add(addBtn).add(removeBtn))
                .add(div().css("type-ctrl-group")
                        .add(undoBtn).add(redoBtn))
                .add(div().css("type-ctrl-group")
                        .add(saveBtn).add(reloadBtn))
                .add(snapCheckbox)
                .element();
    }

    @Override
    public HTMLDivElement element() { return root; }
}
