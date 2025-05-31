package dev.sayaya.handbook.client.interfaces.value;

import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdDialogElement;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import dev.sayaya.ui.elements.SwitchElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLFormElement;
import elemental2.dom.HTMLLabelElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.InputType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.DialogElementBuilder.dialog;
import static dev.sayaya.ui.elements.SwitchElementBuilder.sw;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.*;

@Singleton
public class AttributeEditorDialog implements IsElement<MdDialogElement> {
    @Delegate private final DialogElementBuilder dialog = dialog();
    private final HTMLContainerBuilder<HTMLLabelElement> title = label();
    private final HTMLContainerBuilder<HTMLFormElement> form = form().style("""
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
            align-items: stretch;
            """);
    private final HTMLContainerBuilder<HTMLLabelElement> lblNullable = label("Nullable");
    private final SwitchElementBuilder iptNullable = sw();
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder iptDescription = textField().outlined().label("Description").type(InputType.textarea);
    private final HTMLContainerBuilder<HTMLLabelElement> apply = label();
    private final HTMLContainerBuilder<HTMLLabelElement> close = label();
    private final ButtonElementBuilder.FilledTonalButtonElementBuilder btnApply = button().filledTonal().add(apply);
    private final ButtonElementBuilder.TextButtonElementBuilder btnClose = button().text().add(close);
    private final AttributeEditorElement.AttributeEditorElementFactory editorFactory;
    private AttributeEditorElement editor;
    private Attribute attr;
    private AttributeTypeDefinition def;
    private TypeElement parent;
    private Label label;
    @Inject AttributeEditorDialog(Observable<Label> labels, AttributeEditorElement.AttributeEditorElementFactory editorFactory, ActionManager actionManager) {
        this.editorFactory = editorFactory;
        dialog.headline(title).content(form);
        dialog.actions(div().add(btnClose.form(form)).add(btnApply.form(form)));
        btnApply.on(EventType.click, evt->dialog.close().then(msg-> {
            var attributes = parent.value().attributes();
            if(attributes==null) attributes = java.util.List.of();
            attributes = attributes.stream().map(a->a.equals(attr) ? a.toBuilder()
                            .type(def)
                            .nullable(iptNullable.isSelected())
                            .description(iptDescription.value())
                            .build() : a)
                    .collect(Collectors.toUnmodifiableList());
            var type = parent.value().toBuilder().clearAttributes().attributes(attributes).build();
            actionManager.edit(parent, type);
            return null;
        }));
        btnClose.on(EventType.click, evt->dialog.close().then(msg-> {
            return null;
        }));
        labels.subscribe(this::update);
    }
    public void open(TypeElement parent, Attribute attr) {
        this.parent = parent;
        this.attr = attr;
        this.def = attr.type();
        if (editor != null) editor.element().remove();
        var subject = behavior(def);
        editor = editorFactory.attributeEditorElement(subject);
        subject.subscribe(def->this.def = def);
        form.add(editor).add(iptDescription.style("margin-top: 0.5rem;")).add(div().style("""
                    display: flex;
                    align-items: center;
                    width: 10rem;
                    align-self: center;
                    justify-content: space-between;
                """).add(lblNullable).add(iptNullable));
        if(attr.description()!=null) iptDescription.text(attr.description());
        else iptDescription.text("");
        iptNullable.select(attr.nullable());
        update(label);
        dialog.open();
    }
    private void update(Label labels) {
        this.label = labels;
        if(attr!=null) title.element().innerHTML = Label.findLabelOrDefault(labels, "Edit %type%'s Attribute").replace("%type%", attr.name());
        apply.element().innerHTML = Label.findLabelOrDefault(labels, "Apply");
        close.element().innerHTML = Label.findLabelOrDefault(labels, "Close");
    }
}
