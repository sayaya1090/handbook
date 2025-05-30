package dev.sayaya.handbook.client.interfaces.value;

import dagger.Lazy;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.handbook.client.interfaces.box.TypeElementList;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.rx.Subscription;
import dev.sayaya.ui.elements.*;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.stream.Collectors;

import static dev.sayaya.rx.Observable.timer;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValueElement extends HTMLContainerBuilder<HTMLDivElement> {
    @AssistedInject ValueElement(@Assisted Attribute value, ActionManager actionManager, @Assisted TypeElement parent,
                                 Lazy<TypeElementList> boxes,
                                 AttributeEditorDialog attributeEditor,
                                 BoxReferenceElement.BoxReferenceElementFactory directorFactory,
                                 Lazy<CanvasElement> canvas) {
        this(div(), value, actionManager, parent, boxes, attributeEditor, directorFactory, canvas);
    }
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder title = textField().outlined().css("label");
    private final ButtonElementBuilder.OutlinedButtonElementBuilder type = button().outlined().css("type");
    private final IconButtonElementBuilder.PlainIconButtonElementBuilder btnRem = button().icon().add(icon("remove"));
    private final Lazy<TypeElementList> boxes;
    private final Lazy<CanvasElement> canvas;
    private final BoxReferenceElement.BoxReferenceElementFactory directorFactory;
    private final Attribute target;
    private BoxReferenceElement director;
    private Subscription refSubscription;
    private ValueElement(HTMLContainerBuilder<HTMLDivElement> element, Attribute value, ActionManager actionManager,
                         TypeElement parent, Lazy<TypeElementList> boxes,
                         AttributeEditorDialog attributeEditor,
                         BoxReferenceElement.BoxReferenceElementFactory directorFactory,
                         Lazy<CanvasElement> canvas) {
        super(element.element());
        this.boxes = boxes;
        this.canvas = canvas;
        this.directorFactory = directorFactory;
        this.target = value;
        element.css("property")
                .add(div().style("display: flex; align-items: center;").add(title))
                .add(div().style("display: flex; align-items: center;").add(type).add(btnRem));
        title.onChange(evt->{
            var attributes = parent.value().attributes().stream()
                    .map(a->a.equals(target) ? a.toBuilder().name(title.value()).build() : a)
                    .collect(Collectors.toUnmodifiableList());
            var type = parent.value().toBuilder().clearAttributes().attributes(attributes).build();
            actionManager.edit(parent, type);
        });
        type.onClick(evt->attributeEditor.open(parent, value));
        btnRem.onClick(evt-> {
            var before = parent.value();
            var nextAttributes = before.attributes().stream().filter(a->!a.equals(target)).collect(Collectors.toUnmodifiableList());
            var next = before.toBuilder().clearAttributes().attributes(nextAttributes).height(before.height() - 42).build();
            actionManager.edit(parent, next);
        });
    }
    public void update() {
        title.value(target.name());
        var def = target.type();
        type.text(def.simplify());
        printDirector(def);
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
            // 처음 값 하나는 버리고 이후부터 업데이트
            refSubscription = target.map(TypeElement::value).skip(1).distinctUntilChanged().subscribe(t-> {
                value.referencedType(t.name());
                timer(300, -1).take(1).subscribe(i->update());
            });
        } else if(value.baseType() == AttributeTypeDefinition.AttributeType.Array) printDirector(value.arguments().get(0));
        else if(value.baseType() == AttributeTypeDefinition.AttributeType.Map) printDirector(value.arguments().get(1));
        else director = null;
    }
    @AssistedFactory
    interface ValueElementFactory {
        ValueElement valueElement(Attribute value, TypeElement parent);
    }
}
