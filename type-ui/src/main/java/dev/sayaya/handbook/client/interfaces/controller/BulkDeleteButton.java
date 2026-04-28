package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 선택된 모든 타입을 일괄 삭제하는 Outlined 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link ConfirmDialog}로 삭제 확인을 요청한 뒤,
 * 확인 시 {@link SelectedBoxElement}에서 선택된 모든 타입 키를 조회하고,
 * 각 타입에 대해 {@link DeleteBoxAction}을 실행하여 일괄 삭제 마킹한다.
 * 기존 {@link RemoveTypeButton}과 동일한 패턴이지만 복수 선택에 최적화되어 있다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ActionManager} — 액션 실행</li>
 *   <li>{@link TypeList} — 타입 목록 조회</li>
 *   <li>{@link ChangeTracker} — 삭제 상태 마킹</li>
 *   <li>{@link SelectedBoxElement} — 현재 선택된 타입 키</li>
 *   <li>{@link ConfirmDialog} — 삭제 확인 다이얼로그</li>
 *   <li>{@link LabelProvider} — 다국어 레이블</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 선택이 비어 있으면 아무 동작도 하지 않는다.
 * 삭제 후 선택 상태를 초기화한다.
 * Ctrl+A로 전체 선택 후 Delete 키를 누르면 CanvasElement의 키 핸들러가
 * 동일한 DeleteBoxAction을 실행하므로 키보드와 버튼 양쪽에서 벌크 삭제가 가능하다.</p>
 */
@Singleton
public class BulkDeleteButton implements IsElement<HTMLElement> {
    @Delegate private final ButtonElementBuilder.OutlinedButtonElementBuilder _this;
    private Labels currentLabels = Labels.empty();

    @Inject
    BulkDeleteButton(ActionManager actionManager, TypeList typeList, ChangeTracker tracker,
                     SelectedBoxElement selection, ConfirmDialog confirmDialog,
                     LabelProvider labelProvider) {
        _this = ButtonElementBuilder.button().outlined()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-trash-can"))
                .css("type-ctrl-btn", "type-ctrl-btn-bulk-delete");

        _this.onClick(e -> {
            Set<String> selected = selection.getValue();
            if (selected.isEmpty()) return;

            String headline = currentLabels.getOrDefault("confirm.delete", "Are you sure you want to delete?");
            String yes = currentLabels.getOrDefault("confirm.yes", "Delete");
            String no = currentLabels.getOrDefault("confirm.no", "Cancel");
            confirmDialog.show(headline, new String[]{no, yes}, option -> {
                if (!option.equals(yes)) return;

                List<TypeValue> toDelete = new ArrayList<>();
                for (TypeValue type : typeList.getValue()) {
                    if (selected.contains(type.key())) {
                        toDelete.add(type);
                    }
                }
                for (TypeValue type : toDelete) {
                    actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
                }
                selection.clear();
            });
        });

        labelProvider.subscribe(labels -> {
            currentLabels = labels;
            _this.text(labels.getOrDefault("type.bulk_delete", "Bulk Delete"));
        });
    }
}
