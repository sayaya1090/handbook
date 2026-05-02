package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.usecase.DeleteDocumentAction;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.SelectedRows;
import dev.sayaya.handbook.domain.Document;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 선택 문서 삭제 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link ConfirmDialog}로 삭제 확인을 요청한 뒤,
 * 확인 시 {@link SelectedRows}에서 선택된 항목을 {@link DeleteDocumentAction}으로 제거한다.
 * ActionManager를 경유하므로 Undo/Redo가 지원된다.</p>
 */
@Singleton
public class DeleteButton implements IsElement<elemental2.dom.HTMLElement> {
    @Delegate private final ButtonElementBuilder.OutlinedButtonElementBuilder _this;
    private Labels currentLabels = Labels.empty();

    @Inject
    public DeleteButton(ActionManager actionManager, DocumentList documentList,
                        SelectedRows selectedRows, ConfirmDialog confirmDialog, LabelProvider labelProvider) {
        this._this = ButtonElementBuilder.button().outlined().css("doc-ctrl-btn", "doc-ctrl-btn-delete");
        labelProvider.subscribe(labels -> {
            currentLabels = labels;
            _this.text(labels.getOrDefault("document.delete", "Delete"));
        });
        _this.onClick(e -> {
            Set<Integer> selected = selectedRows.getValue();
            if (selected.isEmpty()) return;

            String headline = currentLabels.getOrDefault("confirm.delete", "Are you sure you want to delete?");
            String yes = currentLabels.getOrDefault("confirm.yes", "Delete");
            String no = currentLabels.getOrDefault("confirm.no", "Cancel");
            confirmDialog.show(headline, new String[]{no, yes}, option -> {
                if (option.equals(yes)) {
                    List<Document> docs = documentList.getValue();
                    List<Document> toDelete = new ArrayList<>();
                    List<Integer> indices = new ArrayList<>(selected);
                    indices.sort(Collections.reverseOrder()); // 인덱스 역순 정렬 (삭제 시 밀림 방지)

                    for (int idx : indices) {
                        if (idx >= 0 && idx < docs.size()) {
                            toDelete.add(docs.get(idx));
                        }
                    }

                    if (!toDelete.isEmpty()) {
                        actionManager.execute(new DeleteDocumentAction(documentList, toDelete, indices));
                        selectedRows.clear();
                    }
                }
            });
        });
    }
}
