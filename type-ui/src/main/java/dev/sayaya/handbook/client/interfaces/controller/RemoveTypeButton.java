package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * 선택된 타입을 삭제하는 Outlined 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link SelectedBoxElement}에서 선택된 타입 키를 조회하고,
 * 각 타입에 대해 {@link DeleteBoxAction}을 실행하여 삭제 마킹한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ActionManager} — 액션 실행</li>
 *   <li>{@link TypeList} — 타입 목록 조회</li>
 *   <li>{@link ChangeTracker} — 삭제 상태 마킹</li>
 *   <li>{@link SelectedBoxElement} — 현재 선택된 타입 키</li>
 *   <li>{@link LabelProvider} — 다국어 레이블</li>
 * </ul></p>
 * <p><b>주의:</b> 다중 선택 시 각 타입별로 별도 DeleteBoxAction이 실행된다.</p>
 */
@Singleton
public class RemoveTypeButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    RemoveTypeButton(ActionManager actionManager, TypeList typeList, ChangeTracker tracker,
                     SelectedBoxElement selection, LabelProvider labelProvider) {
        root = ButtonElementBuilder.button().outlined()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-trash"))
                .css("type-ctrl-btn")
                .element();

        root.addEventListener("click", e -> {
            Set<String> selected = selection.getValue();
            for (TypeValue type : typeList.getValue()) {
                if (selected.contains(type.key())) {
                    actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
                }
            }
        });

        labelProvider.subscribe(labels ->
                root.textContent = labels.getOrDefault("type.remove", "Remove"));
    }

    @Override
    public HTMLElement element() { return root; }
}
