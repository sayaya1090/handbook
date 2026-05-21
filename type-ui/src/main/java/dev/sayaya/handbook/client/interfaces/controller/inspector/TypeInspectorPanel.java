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
import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.*;

@Singleton
public class TypeInspectorPanel implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root = div().css("type-inspector-panel").element();
    private final HTMLElement idLabel = span().css("inspector-value", "type-property-id").element();
    private final HTMLElement versionLabel = span().css("inspector-value", "type-property-version").element();
    private final HTMLElement datesLabel = span().css("inspector-value", "type-property-dates").element();
    private final HTMLDivElement attrList = div().css("inspector-attr-list").element();

    private final SelectedBoxElement selection;
    private final TypeList typeList;
    private final DateCorrectionDialog correctionDialog;
    private final ConfirmDialog confirmDialog;
    private final ActionManager actionManager;
    private final ChangeTracker tracker;
    private Type currentType;

    @Inject
    TypeInspectorPanel(SelectedBoxElement selection, TypeList typeList, DateCorrectionDialog correctionDialog, 
                       ConfirmDialog confirmDialog, ActionManager actionManager, ChangeTracker tracker) {
        this.selection = selection;
        this.typeList = typeList;
        this.correctionDialog = correctionDialog;
        this.confirmDialog = confirmDialog;
        this.actionManager = actionManager;
        this.tracker = tracker;

        HTMLElement closeBtn = ButtonElementBuilder.button()
                .icon(IconElementBuilder.icon().css("fa-solid", "fa-xmark"))
                .element();
        closeBtn.addEventListener("click", e -> selection.clear());
        closeBtn.style.position = "absolute";
        closeBtn.style.top = "12px";
        closeBtn.style.right = "12px";

        root.appendChild(closeBtn);
        root.appendChild(div().css("inspector-header")
                .add(span().css("inspector-label").text("Inspector"))
                .add(span().css("inspector-value").style("font-weight: 600; font-size: 18px;").text("Type Details"))
                .element());
        root.appendChild(div().css("inspector-divider").element());
        root.appendChild(section("Type ID", idLabel));
        root.appendChild(section("Version", versionLabel));
        root.appendChild(section("Validity Period", datesLabel));
        root.appendChild(div().css("inspector-divider").element());
        root.appendChild(section("Attributes", attrList));

        // selection이나 typeList가 변경될 때마다 현재 선택된 타입을 찾아 업데이트
        selection.subscribe(selected -> updateFromList(selected, typeList.getValue()));
        typeList.subscribe(list -> updateFromList(selection.getValue(), list));

        datesLabel.style.cursor = "pointer";
        datesLabel.addEventListener("click", e -> {
            if (currentType != null) {
                correctionDialog.show(currentType, this::handleDateCorrection);
            }
        });
    }

    private void updateFromList(java.util.Set<String> selected, java.util.Set<Type> list) {
        boolean hasSelection = selected != null && !selected.isEmpty();
        if (hasSelection) {
            root.classList.add("visible");
            String key = selected.iterator().next();
            list.stream()
                    .filter(t -> t.key().equals(key))
                    .findFirst()
                    .ifPresent(type -> this.update(type, new ArrayList<>(list)));
        } else {
            root.classList.remove("visible");
            currentType = null;
        }
    }

    private HTMLElement section(String label, HTMLElement valueElement) {
        return div().css("inspector-section")
                .add(span().css("inspector-label").text(label))
                .add(valueElement)
                .element();
    }

    public void update(Type type, List<Type> allTypes) {
        this.currentType = type;
        String key = type.key();
        idLabel.textContent = type.id();
        idLabel.classList.toggle("changed", tracker.getState(key + ":id") == ChangeTracker.ChangeState.CHANGED);
        versionLabel.textContent = type.version();
        versionLabel.classList.toggle("changed", tracker.getState(key + ":version") == ChangeTracker.ChangeState.CHANGED);
        datesLabel.textContent = DateFormatter.formatRange(type);
        datesLabel.classList.toggle("changed", tracker.getState(key + ":dates") == ChangeTracker.ChangeState.CHANGED);
        
        attrList.innerHTML = "";
        if (type.attributes() != null) {
            for (Attribute attr : type.attributes()) {
                String typeName = attr.type() != null ? attr.type().type() : "text";
                boolean isChanged = tracker.getState(key + ":attr:" + attr.name()) == ChangeTracker.ChangeState.CHANGED;
                HTMLElement item = div().css("inspector-attr-item")
                        .add(div().css("inspector-attr-info")
                                .add(div().css("inspector-attr-main")
                                        .add(span().css("inspector-attr-icon", "fa-solid", getIcon(typeName)))
                                        .add(span().css("inspector-attr-name").text(attr.name())))
                                .add(span().css("inspector-attr-desc").text(attr.description() != null ? attr.description() : "")))
                        .add(span().css("inspector-attr-type").text(typeName))
                        .element();
                if (isChanged) item.classList.add("changed");
                attrList.appendChild(item);
            }
        }
    }

    private String getIcon(String type) {
        switch (type) {
            case "number": return "fa-hashtag";
            case "date": return "fa-calendar-days";
            case "enum": return "fa-list-ul";
            case "bool": return "fa-toggle-on";
            case "array": return "fa-layer-group";
            case "map": return "fa-diagram-project";
            case "file": return "fa-file";
            case "document": return "fa-file-invoice";
            default: return "fa-font";
        }
    }

    private void handleDateCorrection(DateCorrectionDialog.DateResult result) {
        Type target = currentType;
        boolean effectChanged = Math.abs(target.effectDateTime() - result.effect()) > 0.1;
        boolean expireChanged = Math.abs(target.expireDateTime() - result.expire()) > 0.1;

        if (!effectChanged && !expireChanged && result.proposal() == null) return;

        List<dev.sayaya.handbook.domain.Action> actions = new ArrayList<>();
        if (effectChanged || expireChanged) {
            actions.add(new EditTBoxDateAction(typeList, tracker, target, result.effect(), result.expire()));
        }

        if (result.proposal() != null) {
            Type other = typeList.getValue().stream().filter(t -> t.key().equals(result.proposal().targetKey())).findFirst().orElse(null);
            if (other != null) {
                actions.add(new EditTBoxDateAction(typeList, tracker, other, result.proposal().newStart(), result.proposal().newEnd()));
            }
        }

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
                    if (prev != null) actions.add(new EditTBoxDateAction(typeList, tracker, prev, prev.effectDateTime(), result.effect()));
                    if (next != null) actions.add(new EditTBoxDateAction(typeList, tracker, next, result.expire(), next.expireDateTime()));
                }
                
                if (actions.size() == 1) {
                    actionManager.execute(actions.get(0));
                } else if (actions.size() > 1) {
                    actionManager.execute(new ComplexAction(actions.toArray(new dev.sayaya.handbook.domain.Action[0])));
                }
            });
        } else {
            if (actions.size() == 1) {
                actionManager.execute(actions.get(0));
            } else if (actions.size() > 1) {
                actionManager.execute(new ComplexAction(actions.toArray(new dev.sayaya.handbook.domain.Action[0])));
            }
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}
