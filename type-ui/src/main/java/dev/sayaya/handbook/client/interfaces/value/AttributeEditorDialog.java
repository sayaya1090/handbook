package dev.sayaya.handbook.client.interfaces.value;

import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Label;
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

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.DialogElementBuilder.dialog;
import static dev.sayaya.ui.elements.SwitchElementBuilder.sw;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static elemental2.dom.DomGlobal.alert;
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
    private ValueUpdater updater;
    private Label label;
    @Inject AttributeEditorDialog(Observable<Label> labels, AttributeEditorElement.AttributeEditorElementFactory editorFactory) {
        this.editorFactory = editorFactory;
        dialog.headline(title).content(form);
        dialog.actions(div().add(btnClose.form(form)).add(btnApply.form(form)));
        btnApply.on(EventType.click, evt->dialog.close().then(msg-> {
            if(updater != null) updater.update(def);
            if(iptDescription.value() != null && !iptDescription.value().isEmpty()) attr.description(iptDescription.value());
            else attr.description(null);
            attr.nullable(iptNullable.isSelected());
            return null;
        }));
        btnClose.on(EventType.click, evt->dialog.close().then(msg-> {
            return null;
        }));
        labels.subscribe(this::update);
    }
    public void open(Attribute attr,ValueUpdater updater) {
        this.attr = attr;
        this.def = attr.type();
        this.updater = updater;
        if (editor != null) editor.element().remove();
        editor = editorFactory.attributeEditorElement(def);
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
        title.element().innerHTML = Label.findLabelOrDefault(labels, "Edit %type%'s Attribute").replace("%type%", attr.name());
        apply.element().innerHTML = Label.findLabelOrDefault(labels, "Apply");
        close.element().innerHTML = Label.findLabelOrDefault(labels, "Close");
    }
}
