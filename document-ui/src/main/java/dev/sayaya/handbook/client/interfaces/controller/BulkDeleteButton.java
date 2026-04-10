package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.client.usecase.DeleteDocumentAction;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.SelectedRows;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jboss.elemento.Elements.button;

/**
 * 선택된 모든 문서를 일괄 삭제하는 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link ConfirmDialog}로 삭제 확인을 요청한 뒤,
 * 확인 시 {@link SelectedRows}에서 선택된 행 인덱스를 조회하고,
 * 해당 문서들을 {@link DeleteDocumentAction}으로 일괄 삭제한다.
 * ActionManager를 경유하므로 Undo/Redo가 지원된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ActionManager} — 액션 실행 및 Undo/Redo 스택 관리</li>
 *   <li>{@link DocumentList} — 삭제 대상 문서 목록 조회</li>
 *   <li>{@link SelectedRows} — 현재 선택된 행 인덱스 집합</li>
 *   <li>{@link ConfirmDialog} — 삭제 확인 다이얼로그</li>
 *   <li>{@link LabelProvider} — 버튼 레이블 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 선택이 비어 있으면 아무 동작도 하지 않는다.
 * 삭제 후 선택 상태를 초기화한다.</p>
 */
@Singleton
public class BulkDeleteButton implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLElement element;
    private Labels currentLabels = Labels.empty();

    @Inject
    public BulkDeleteButton(ActionManager actionManager, DocumentList documentList,
                            SelectedRows selectedRows, ConfirmDialog confirmDialog,
                            LabelProvider labelProvider) {
        this.element = button().css("doc-ctrl-btn", "doc-ctrl-btn-bulk-delete").element();
        labelProvider.subscribe(labels -> {
            currentLabels = labels;
            element.textContent = labels.getOrDefault("document.bulk_delete", "Bulk Delete");
        });

        element.addEventListener("click", e -> {
            Set<Integer> selected = selectedRows.getValue();
            if (selected.isEmpty()) return;

            String headline = currentLabels.getOrDefault("confirm.delete", "Are you sure you want to delete?");
            String yes = currentLabels.getOrDefault("confirm.yes", "Delete");
            String no = currentLabels.getOrDefault("confirm.no", "Cancel");
            confirmDialog.show(headline, new String[]{no, yes}, option -> {
                if (!option.equals(yes)) return;

                List<DocumentValue> docs = documentList.getValue();
                List<DocumentValue> toDelete = new ArrayList<>();
                List<Integer> indices = new ArrayList<>(selected);
                indices.sort(Integer::compareTo);

                for (int idx : indices) {
                    if (idx >= 0 && idx < docs.size()) {
                        toDelete.add(docs.get(idx));
                    }
                }

                if (!toDelete.isEmpty()) {
                    actionManager.execute(new DeleteDocumentAction(documentList, toDelete, indices));
                    selectedRows.clear();
                }
            });
        });
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
