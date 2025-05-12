package dev.sayaya.handbook.client.interfaces.value;

import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdDialogElement;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import elemental2.dom.HTMLFormElement;
import elemental2.dom.HTMLLabelElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.DialogElementBuilder.dialog;
import static elemental2.dom.DomGlobal.alert;
import static org.jboss.elemento.Elements.*;

@Singleton
public class AttributeEditorDialog implements IsElement<MdDialogElement> {
    @Delegate private final DialogElementBuilder dialog = dialog();
    private final HTMLContainerBuilder<HTMLLabelElement> title = label();
    private final HTMLContainerBuilder<HTMLFormElement> form = form();
    private final HTMLContainerBuilder<HTMLLabelElement> apply = label();
    private final HTMLContainerBuilder<HTMLLabelElement> close = label();
    private final ButtonElementBuilder.FilledTonalButtonElementBuilder btnApply = button().filledTonal().add(apply);
    private final ButtonElementBuilder.TextButtonElementBuilder btnClose = button().text().add(close);
    private final AttributeEditorElement.AttributeEditorElementFactory editorFactory;
    private AttributeEditorElement editor;
    @Inject AttributeEditorDialog(Observable<Label> labels, AttributeEditorElement.AttributeEditorElementFactory editorFactory) {
        this.editorFactory = editorFactory;
        dialog.headline(title).content(form);
        dialog.actions(div().add(btnClose.form(form)).add(btnApply.form(form)));
        btnApply.on(EventType.click, evt->dialog.close().then(msg-> {
            alert(msg);
            return null;
        }));
        btnClose.on(EventType.click, evt->dialog.close().then(msg-> {
            alert(msg);
            return null;
        }));
        labels.subscribe(this::update);
    }
    public void open(AttributeTypeDefinition def) {
        if (editor != null) editor.element().remove();
        editor = editorFactory.attributeEditorElement(def);
        form.add(editor);
        dialog.open();
    }
    private void update(Label labels) {
        title.element().innerHTML = Label.findLabelOrDefault(labels, "Edit %type%'s Attribute");
        apply.element().innerHTML = Label.findLabelOrDefault(labels, "Apply");
        close.element().innerHTML = Label.findLabelOrDefault(labels, "Close");
    }
}
