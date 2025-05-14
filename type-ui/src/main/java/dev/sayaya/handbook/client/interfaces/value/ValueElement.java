package dev.sayaya.handbook.client.interfaces.value;

import dagger.Lazy;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.interfaces.box.BoxElement;
import dev.sayaya.handbook.client.interfaces.box.BoxElementList;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.rx.Subscription;
import dev.sayaya.ui.elements.*;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;

import static dev.sayaya.rx.Observable.timer;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValueElement extends HTMLContainerBuilder<HTMLDivElement> implements ValueUpdater {
    @AssistedInject ValueElement(@Assisted Attribute value, ActionManager actionManager, @Assisted BoxElement parent,
                                 Lazy<BoxElementList> boxes,
                                 AttributeEditorDialog attributeEditor,
                                 BoxReferenceElement.BoxReferenceElementFactory directorFactory,
                                 Lazy<CanvasElement> canvas) {
        this(div(), value, actionManager, parent, boxes, attributeEditor, directorFactory, canvas);
    }
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder title = textField().outlined().css("label");
    private final ButtonElementBuilder.OutlinedButtonElementBuilder type = button().outlined().css("type");
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder btnRem = button().icon().add(icon("remove"));
    private final Lazy<BoxElementList> boxes;
    private final Lazy<CanvasElement> canvas;
    private final BoxReferenceElement.BoxReferenceElementFactory directorFactory;
    private BoxReferenceElement director;
    private Subscription refSubscription;
    private ValueElement(HTMLContainerBuilder<HTMLDivElement> element, Attribute value, ActionManager actionManager,
                         BoxElement parent, Lazy<BoxElementList> boxes,
                         AttributeEditorDialog attributeEditor,
                         BoxReferenceElement.BoxReferenceElementFactory directorFactory,
                         Lazy<CanvasElement> canvas) {
        super(element.element());
        this.boxes = boxes;
        this.canvas = canvas;
        this.directorFactory = directorFactory;
        element.css("property")
                .add(div().style("display: flex; align-items: center;").add(title))
                .add(div().style("display: flex; align-items: center;").add(type).add(btnRem));
        title.onChange(evt->target.name(title.value()));
        type.onClick(evt->attributeEditor.open(value, this));
        btnRem.onClick(evt-> actionManager.removeValue(parent, value));
    }
    private Attribute target;
    public void update(Attribute value) {
        this.target = value;
        title.value(value.name());
        update(value.type());
    }
    @Override
    public void update(AttributeTypeDefinition value) {
        type.text(value.toString());
        printDirector(value);
    }
    private void printDirector(AttributeTypeDefinition value) {
        if(director!=null) {
            director.clear();
            director.element().remove();
        }
        if(refSubscription != null) refSubscription.unsubscribe();
        if(value.baseType() == AttributeTypeDefinition.AttributeType.Document) {
            var ref = value.referencedType();
            var target = boxes.get().find(ref);
            director = directorFactory.director(this, target);
            canvas.get().add(director.element());
            refSubscription = target.subscribe(t-> {
                value.referencedType(t.box().id());
                timer(300, -1).subscribe(i->update(this.target));
            });
        } else if(value.baseType() == AttributeTypeDefinition.AttributeType.Array) printDirector(value.arguments().get(0));
        else if(value.baseType() == AttributeTypeDefinition.AttributeType.Map) printDirector(value.arguments().get(1));
        else director = null;
    }
    @AssistedFactory
    interface ValueElementFactory {
        ValueElement valueElement(Attribute value, BoxElement parent);
    }
}
