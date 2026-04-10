package dev.sayaya.handbook.client.interfaces.controller;

import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 캔버스 상단 툴바 컨테이너.
 *
 * <p><b>책임:</b> 기간 이동(Before/After), 타입 추가/삭제, Undo/Redo,
 * 저장/새로고침 버튼, 스냅 체크박스, 모드 토글 등 모든 컨트롤러 버튼을 수평 배치한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link BeforeButton}, {@link AfterButton} — 레이아웃 기간 탐색</li>
 *   <li>{@link AddTypeButton}, {@link RemoveTypeButton} — 타입 CRUD</li>
 *   <li>{@link UndoButton}, {@link RedoButton} — Undo/Redo</li>
 *   <li>{@link SaveButton}, {@link ReloadButton} — 저장/새로고침</li>
 *   <li>{@link SnapCheckbox} — 그리드 스냅 토글</li>
 *   <li>{@link ModeToggleButton} — LAYOUT/TYPE 모드 전환</li>
 * </ul></p>
 * <p><b>주의:</b> CSS 클래스 "type-controller"로 스타일링된다. 버튼은 "type-ctrl-group"으로 그룹핑.</p>
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
