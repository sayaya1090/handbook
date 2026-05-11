package dev.sayaya.handbook.client.interfaces.controller.inspector;

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
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.div;

@Singleton
public class TypeBottomSheet implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root = div().css("type-bottom-sheet").element();
    private final HTMLElement idLabel = div().css("type-property-id").element();
    private final HTMLElement versionLabel = div().css("type-property-version").element();
    private final HTMLElement datesLabel = div().css("type-property-dates").element();

    private final SelectedBoxElement selection;
    private final TypeList typeList;
    private final DateCorrectionDialog correctionDialog;
    private final ConfirmDialog confirmDialog;
    private final ActionManager actionManager;
    private final ChangeTracker tracker;
    private Type currentType;

    @Inject
    TypeBottomSheet(SelectedBoxElement selection, TypeList typeList, DateCorrectionDialog correctionDialog, 
                    ConfirmDialog confirmDialog, ActionManager actionManager, ChangeTracker tracker) {
        this.selection = selection;
        this.typeList = typeList;
        this.correctionDialog = correctionDialog;
        this.confirmDialog = confirmDialog;
        this.actionManager = actionManager;
        this.tracker = tracker;

        root.appendChild(idLabel);
        root.appendChild(versionLabel);
        root.appendChild(datesLabel);

        selection.subscribe(selected -> {
            boolean hasSelection = selected != null && !selected.isEmpty();
            if (hasSelection) {
                root.classList.add("visible");
                String key = selected.iterator().next();
                typeList.getValue().stream()
                        .filter(t -> t.key().equals(key))
                        .findFirst()
                        .ifPresent(type -> this.update(type, new ArrayList<>(typeList.getValue())));
            } else {
                root.classList.remove("visible");
                currentType = null;
            }
        });

        datesLabel.addEventListener("click", e -> {
            if (currentType != null) {
                correctionDialog.show(currentType, this::handleDateCorrection);
            }
        });
    }

    public void update(Type type, List<Type> allTypes) {
        this.currentType = type;
        idLabel.textContent = "ID: " + type.id();
        versionLabel.textContent = "Version: " + type.version();
        datesLabel.textContent = DateFormatter.formatRange(type);
    }

    private void handleDateCorrection(DateCorrectionDialog.DateResult result) {
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
                    List<Action> actions = new ArrayList<>();
                    actions.add(new EditTBoxDateAction(typeList, tracker, target, result.effect(), result.expire()));
                    if (prev != null) actions.add(new EditTBoxDateAction(typeList, tracker, prev, prev.effectDateTime(), result.effect()));
                    if (next != null) actions.add(new EditTBoxDateAction(typeList, tracker, next, result.expire(), next.expireDateTime()));
                    actionManager.execute(new ComplexAction(actions.toArray(new Action[0])));
                } else {
                    actionManager.execute(new EditTBoxDateAction(typeList, tracker, target, result.effect(), result.expire()));
                }
            });
        } else {
            actionManager.execute(new EditTBoxDateAction(typeList, tracker, target, result.effect(), result.expire()));
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}