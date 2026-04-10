package dev.sayaya.handbook.client.interfaces.controller;

import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 상단 툴바 컨트롤러.
 *
 * <p><b>책임:</b> 타입 탭, CRUD 버튼(추가/삭제/저장), Undo/Redo 버튼을 하나의 툴바로
 * 조합하여 문서 편집 화면 상단에 배치한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link TypeTabsElement} — 타입 선택 탭</li>
 *   <li>{@link AddButton} — 문서 추가</li>
 *   <li>{@link DeleteButton} — 문서 삭제</li>
 *   <li>{@link SaveButton} — 변경사항 저장</li>
 *   <li>{@link UndoButton} — 실행 취소</li>
 *   <li>{@link RedoButton} — 다시 실행</li>
 *   <li>{@link BulkDeleteButton} — 선택 문서 일괄 삭제</li>
 *   <li>{@link BulkStatusButton} — 선택 문서 일괄 상태 변경</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 모든 하위 버튼은 Dagger에 의해 주입되며, 레이아웃 순서는
 * 생성자 내 add() 호출 순서로 결정된다.</p>
 */
@Singleton
public class ControllerElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;

    @Inject
    public ControllerElement(TypeTabsElement tabs, AddButton addButton, DeleteButton deleteButton,
                              SaveButton saveButton, UndoButton undoButton, RedoButton redoButton,
                              BulkDeleteButton bulkDeleteButton, BulkStatusButton bulkStatusButton) {
        this.element = div().css("doc-controller")
                .add(tabs)
                .add(div().css("doc-ctrl-actions")
                        .add(addButton)
                        .add(deleteButton)
                        .add(undoButton)
                        .add(redoButton)
                        .add(saveButton)
                        .add(bulkDeleteButton)
                        .add(bulkStatusButton))
                .element();
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
