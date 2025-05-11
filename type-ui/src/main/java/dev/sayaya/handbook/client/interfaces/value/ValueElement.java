package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.AttributeTypeList;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.Subject;
import dev.sayaya.ui.elements.CheckboxElementBuilder;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.SelectElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static dev.sayaya.ui.elements.SelectElementBuilder.select;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValueElement extends HTMLContainerBuilder<HTMLDivElement> {
    @AssistedInject ValueElement(@Assisted Attribute value, AttributeTypeList typeList, ActionManager actionManager, @Assisted UpdatableBox parent) {
        this(div(), value, typeList, actionManager, parent);
    }
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder title = textField().outlined().css("label");
    private final CheckboxElementBuilder nullable = CheckboxElementBuilder.checkbox();
    //private final SelectElementBuilder.OutlinedSelectElementBuilder type = select().outlined().css("type");
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder btnRem = button().icon().add(icon("remove"));
    private final Subscription typeListSubscription;
    private ValueElement(HTMLContainerBuilder<HTMLDivElement> element, Attribute value, AttributeTypeList typeList, ActionManager actionManager, UpdatableBox parent) {
        super(element.element());
        update(value);
        typeListSubscription = typeList.distinctUntilChanged().subscribe(this::update);
        element.css("property")
                .add(div().style("display: flex; align-items: center;").add(nullable).add(title))
                .add(div().style("display: flex; align-items: center;").add(btnRem));
        nullable.onChange(evt->target.nullable(!nullable.isSelected()));
        title.onChange(evt->target.name(title.value()));
        /*type.onChange(evt->{
            target.type(type.element().value);
            updateTypes();
        });*/
        btnRem.onClick(evt->{
            actionManager.removeValue(parent, value);
            typeListSubscription.unsubscribe();
        });
    }
    private Attribute target;
    private void update(Attribute value) {
        this.target = value;
        nullable.select(!value.nullable());
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
