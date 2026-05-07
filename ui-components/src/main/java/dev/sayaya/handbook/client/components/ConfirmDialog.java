package dev.sayaya.handbook.client.components;

import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import elemental2.dom.*;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;

/**
 * 범용 확인 다이얼로그 컴포넌트.
 */
public class ConfirmDialog implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private static int idCounter = 0;

    public ConfirmDialog() {
        root = div().css("ui-confirm-wrapper").element();
    }

    public void show(String headline, String[] options, Consumer<String> onSelect) {
        if (root.parentNode == null) {
            DomGlobal.document.body.appendChild(root);
        }
        root.innerHTML = "";
        String headlineId = "ui-confirm-headline-" + (++idCounter);

        HTMLDivElement actionsContainer = div().css("ui-confirm-actions").element();
        DialogElementBuilder dialogBuilder = DialogElementBuilder.dialog()
                .css("ui-confirm-dialog")
                .headline(headline)
                .actions(div().add(actionsContainer));

        for (String option : options) {
            HTMLElement btn = ButtonElementBuilder.button().text()
                    .text(option)
                    .css("ui-confirm-option")
                    .on(EventType.click, e -> {
                        dialogBuilder.open(false);
                        if (onSelect != null) onSelect.accept(option);
                    })
                    .element();
            actionsContainer.appendChild(btn);
        }

        HTMLElement dialogEl = dialogBuilder.element();
        dialogEl.setAttribute("role", "alertdialog");
        dialogEl.setAttribute("aria-labelledby", headlineId);

        NodeList<elemental2.dom.Element> headlines = dialogEl.querySelectorAll("[slot=headline], .headline");
        if (headlines.length > 0) headlines.getAt(0).id = headlineId;
        else dialogEl.id = headlineId;

        dialogEl.addEventListener("keydown", evt -> {
            KeyboardEvent ke = Js.cast(evt);
            if ("Escape".equals(ke.key)) dialogBuilder.open(false);
        });

        setupKeyboardTrap(actionsContainer, dialogBuilder);
        root.appendChild(dialogEl);
        dialogBuilder.open(true);

        DomGlobal.setTimeout(e -> {
            NodeList<elemental2.dom.Element> btns = actionsContainer.querySelectorAll("button, [role=button], md-text-button");
            if (btns.length > 0) Js.<HTMLElement>cast(btns.getAt(0)).focus();
        }, 100);
    }

    private void setupKeyboardTrap(HTMLDivElement container, DialogElementBuilder dialogBuilder) {
        container.addEventListener("keydown", evt -> {
            KeyboardEvent ke = Js.cast(evt);
            if (!"Tab".equals(ke.key)) return;
            NodeList<elemental2.dom.Element> focusable = container.querySelectorAll("button, [role=button], [tabindex], md-text-button");
            if (focusable.length == 0) return;
            HTMLElement first = Js.cast(focusable.getAt(0));
            HTMLElement last = Js.cast(focusable.getAt(focusable.length - 1));
            elemental2.dom.Element active = DomGlobal.document.activeElement;
            if (ke.shiftKey) {
                if (active == first) {
                    ke.preventDefault();
                    last.focus();
                }
            } else {
                if (active == last) {
                    ke.preventDefault();
                    first.focus();
                }
            }
        });
    }

    @Override
    public HTMLDivElement element() { return root; }
}
