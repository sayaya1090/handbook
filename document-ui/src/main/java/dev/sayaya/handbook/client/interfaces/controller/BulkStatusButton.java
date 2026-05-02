package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.ChangeStatusAction;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.SelectedRows;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLSelectElement;
import org.jboss.elemento.Elements;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jboss.elemento.Elements.*;

/**
 * 선택된 문서들의 상태를 일괄 변경하는 드롭다운 버튼.
 *
 * <p><b>책임:</b> DRAFT/REVIEW/PUBLISHED 상태를 선택할 수 있는 select 드롭다운을 표시하고,
 * 변경 시 {@link SelectedRows}에서 선택된 행들의 상태를 {@link ChangeStatusAction}으로 일괄 변경한다.
 * ActionManager를 경유하므로 Undo/Redo가 지원된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ActionManager} — 액션 실행 및 Undo/Redo 스택 관리</li>
 *   <li>{@link DocumentList} — 상태 변경 대상 문서 목록 조회</li>
 *   <li>{@link SelectedRows} — 현재 선택된 행 인덱스 집합</li>
 *   <li>{@link LabelProvider} — 드롭다운 레이블 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 선택이 비어 있으면 상태 변경이 수행되지 않는다.
 * 드롭다운 변경 후 선택 상태를 초기화한다.</p>
 */
@Singleton
public class BulkStatusButton implements IsElement<HTMLElement> {
    private static final String[] STATUSES = {"DRAFT", "REVIEW", "PUBLISHED"};

    private final HTMLElement element;

    @Inject
    public BulkStatusButton(ActionManager actionManager, DocumentList documentList,
                            SelectedRows selectedRows, LabelProvider labelProvider) {
        var label = span().css("doc-ctrl-bulk-status-label");
        var labelEl = label.element();

        var selectBuilder = select().css("doc-ctrl-bulk-status-select");
        HTMLSelectElement selectEl = selectBuilder.element();
        selectEl.appendChild(Elements.option().attr("value", "").text("---").element());
        for (String status : STATUSES) {
            selectEl.appendChild(Elements.option().attr("value", status).text(status).element());
        }
        selectBuilder.on(EventType.change, e -> {
            String newStatus = selectEl.value;
            DomGlobal.console.log(newStatus);
            if (newStatus == null || newStatus.isEmpty()) return;

            Set<Integer> selected = selectedRows.getValue();
            DomGlobal.console.log(selected.stream().toList());
            if (selected.isEmpty()) {
                selectEl.value = "";
                return;
            }

            List<Integer> indices = new ArrayList<>(selected);
            indices.sort(Integer::compareTo);
            actionManager.execute(new ChangeStatusAction(documentList, indices, newStatus));
            selectedRows.clear();
            selectEl.value = "";
        });

        labelProvider.subscribe(labels ->
                labelEl.textContent = labels.getOrDefault("document.bulk_status", "Status"));

        this.element = div().css("doc-ctrl-bulk-status")
                .add(label)
                .add(selectBuilder)
                .element();
    }

    @Override
    public HTMLElement element() { return element; }
}
