package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.interfaces.editor.DateCorrectionDialog;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.DateFormatter;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.ComplexAction;
import dev.sayaya.handbook.client.usecase.action.EditTBoxDateAction;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 선택된 타입의 상세 정보를 표시하는 바 (Property Bar).
 * 
 * <p><b>책임:</b> 캔버스에서 하나의 타입이 선택되었을 때 ID, 버전, 유효기간을 표시한다.
 * 유효기간 클릭 시 날짜 수정 기능을 제공하고, '새 버전 생성' 액션의 진입점이 된다.</p>
 */
@Singleton
public class TypePropertyBar implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLElement idLabel = span().css("type-property-id").element();
    private final HTMLElement versionLabel = span().css("type-property-version").element();
    private final HTMLElement datesLabel = span().css("type-property-dates").element();

    private final TypeList typeList;
    private final ConfirmDialog confirmDialog;
    private final ActionManager actionManager;
    private final ChangeTracker tracker;
    private Type currentType;

    @Inject
    TypePropertyBar(SelectedBoxElement selection, TypeList typeList, LabelProvider labelProvider,
                    DateCorrectionDialog correctionDialog,
                    ActionManager actionManager, ChangeTracker tracker, ConfirmDialog confirmDialog) {
        this.typeList = typeList;
        this.confirmDialog = confirmDialog;
        this.actionManager = actionManager;
        this.tracker = tracker;
        
        this.root = div().css("type-property-bar", "visible", "type-fade-item")
                .add(idLabel)
                .add(span().css("type-property-divider").text("|"))
                .add(versionLabel)
                .add(span().css("type-property-divider").text("|"))
                .add(datesLabel)
                .element();

        selection.subscribe(selected -> this.refresh(selected, typeList.getValue()));
        typeList.subscribe(types -> this.refresh(selection.getValue(), types));
        
        // 초기 상태 설정
        clearLabels();

        // 전체 영역을 클릭할 수 있도록 root에 이벤트 바인딩 (UX 개선)
        root.addEventListener("click", e -> {
            if (currentType != null) {
                correctionDialog.show(currentType, this::handleDateCorrection);
            }
        });
    }

    private void clearLabels() {
        idLabel.textContent = "-";
        versionLabel.textContent = "-";
        datesLabel.textContent = "-";
    }

    private void handleDateCorrection(DateCorrectionDialog.DateResult result) {
        // ... (이전과 동일)
        Type target = currentType;
        boolean effectChanged = Math.abs(target.effectDateTime() - result.effect()) > 0.1;
        boolean expireChanged = Math.abs(target.expireDateTime() - result.expire()) > 0.1;

        if (!effectChanged && !expireChanged) return;

        Type adjacentPrev = null;
        Type adjacentNext = null;

        if (effectChanged) {
            adjacentPrev = typeList.getValue().stream()
                    .filter(t -> t.id().equals(target.id()) && Math.abs(t.expireDateTime() - target.effectDateTime()) < 0.1)
                    .findFirst().orElse(null);
        }
        if (expireChanged) {
            adjacentNext = typeList.getValue().stream()
                    .filter(t -> t.id().equals(target.id()) && Math.abs(t.effectDateTime() - target.expireDateTime()) < 0.1)
                    .findFirst().orElse(null);
        }

        if (adjacentPrev != null || adjacentNext != null) {
            String message = "Synchronize Adjacent Versions - Do you want to adjust the adjacent version(s) to match the new date and prevent gaps in the timeline?";
            
            final Type prev = adjacentPrev;
            final Type next = adjacentNext;
            
            confirmDialog.show(message, new String[]{"Yes", "No"}, option -> {
                if ("Yes".equals(option)) {
                    // Yes: 복합 액션 생성
                    List<Action> actions = new ArrayList<>();
                    actions.add(new EditTBoxDateAction(typeList, tracker, target, result.effect(), result.expire()));
                    if (prev != null) actions.add(new EditTBoxDateAction(typeList, tracker, prev, prev.effectDateTime(), result.effect()));
                    if (next != null) actions.add(new EditTBoxDateAction(typeList, tracker, next, result.expire(), next.expireDateTime()));
                    actionManager.execute(new ComplexAction(actions.toArray(new Action[0])));
                } else {
                    // No: 단일 액션
                    actionManager.execute(new EditTBoxDateAction(typeList, tracker, target, result.effect(), result.expire()));
                }
            });
        } else {
            actionManager.execute(new EditTBoxDateAction(typeList, tracker, target, result.effect(), result.expire()));
        }
    }

    private void refresh(Set<String> selectedKeys, Set<Type> types) {
        if (selectedKeys.size() == 1) {
            String key = selectedKeys.iterator().next();
            types.stream()
                    .filter(t -> t.key().equals(key))
                    .findFirst()
                    .ifPresent(this::update);
        } else {
            currentType = null;
            clearLabels();
        }
    }

    private void update(Type type) {
        this.currentType = type;
        idLabel.textContent = type.id();
        versionLabel.textContent = type.version();
        datesLabel.textContent = DateFormatter.formatRange(type);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
