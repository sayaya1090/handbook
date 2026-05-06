package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.interfaces.api.LayoutApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 타입 편집기 상단바.
 * 
 * <p><b>책임:</b> 시스템 전반의 글로벌 액션(Undo, Save 등)과 
 * 뷰 설정(스냅, 기간 이동)을 한데 모아 제공한다.</p>
 */
@Singleton
public class StatusHeaderElement implements IsElement<HTMLDivElement> {
    private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("type-status-header");

    @Inject
    StatusHeaderElement(ModeToggleButton modeToggle,
                        BeforeButton beforeBtn, AfterButton afterBtn,
                        UndoButton undoBtn, RedoButton redoBtn,
                        SaveButton saveBtn, ReloadButton reloadBtn,
                        SnapCheckbox snapCheckbox) {
        _this.add(modeToggle)
             .add(div().css("type-ctrl-group").add(beforeBtn).add(afterBtn))
             .add(div().css("type-ctrl-group").add(undoBtn).add(redoBtn))
             .add(div().css("type-ctrl-group").add(saveBtn).add(reloadBtn))
             .add(snapCheckbox);
    }

    @Override
    public HTMLDivElement element() { return _this.element(); }
}
