package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import dev.sayaya.rx.Subscription;
import dev.sayaya.ui.elements.*;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValueElement extends HTMLContainerBuilder<HTMLDivElement> {
    @AssistedInject ValueElement(@Assisted Attribute value, ActionManager actionManager, @Assisted UpdatableBox parent, AttributeEditorDialog attributeEditor) {
        this(div(), value, actionManager, parent, attributeEditor);
    }
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder title = textField().outlined().css("label");
    private final ButtonElementBuilder.OutlinedButtonElementBuilder type = button().outlined().text("Value").css("type");
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder btnRem = button().icon().add(icon("remove"));
    private ValueElement(HTMLContainerBuilder<HTMLDivElement> element, Attribute value, ActionManager actionManager, UpdatableBox parent, AttributeEditorDialog attributeEditor) {
        super(element.element());
        update(value);
        element.css("property")
                .add(div().style("display: flex; align-items: center;").add(title))
                .add(div().style("display: flex; align-items: center;").add(type).add(btnRem));
        title.onChange(evt->target.name(title.value()));
        type.onClick(evt->attributeEditor.open(value.type()));
        btnRem.onClick(evt-> actionManager.removeValue(parent, value));
    }
    private Attribute target;
    private void update(Attribute value) {
        this.target = value;
        title.value(value.name());
        //type.element().value = value.type();
    }
    private void update(String[] types) {
        //update(type, types);
    }
    private static void update(SelectElementBuilder.OutlinedSelectElementBuilder elem, String[] types) {
        elem.removeAllOptions();
        for(var t: types) elem.option().value(t).headline(t);
    }
    @AssistedFactory
    interface ValueElementFactory {
        ValueElement valueElement(Attribute value, UpdatableBox parent);
    }
}
