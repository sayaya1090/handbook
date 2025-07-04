package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.ui.dom.MdIconButtonElement;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.MouseEvent;
import lombok.experimental.Delegate;

import javax.inject.Inject;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

public class AfterButton implements IconButtonElementBuilder<MdIconButtonElement, IconButtonElementBuilder.PlainIconButtonElementBuilder> {
    @Delegate private final PlainIconButtonElementBuilder btn = button().icon().add(
            IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-caret-right")
    );
    private final TypeProvider typeProvider;
    private final TypeList types;
    @Inject AfterButton(TypeProvider typeProvider, TypeList types) {
        this.typeProvider = typeProvider;
        this.types = types;
        typeProvider.subscribe(this::update);
        btn.onClick(this::act);
    }
    private void act(MouseEvent mouseEvent) {
        Type current = typeProvider.getValue();
        Type next = types.getValue().get(current.id()).get(current.next());
        if(next!=null) typeProvider.next(next);
    }
    private void update(Type type) {
        btn.element().disabled = type==null || type.next()==null || type.next().isEmpty();
    }
}
