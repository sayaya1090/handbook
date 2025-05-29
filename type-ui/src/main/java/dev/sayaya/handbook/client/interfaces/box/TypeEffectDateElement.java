package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.ui.dom.MdTextFieldElement;
import org.jboss.elemento.IsElement;

import java.util.Date;

class TypeEffectDateElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    private final TypeDateValueElement ipt;
    private final TypeElement parent;
    private final ActionManager actionManager;
    @AssistedInject TypeEffectDateElement(@Assisted TypeElement parent, ActionManager actionManager, TypeDateValueElement.TypeDateValueElementFactory factory) {
        ipt = factory.create("Effect Date", this::effectDateTime);
        this.parent = parent;
        this.actionManager = actionManager;
        parent.subscribe(evt->ipt.update(parent.value().effectDateTime()));
    }
    private void effectDateTime(Date value) {
        var next = parent.value().toBuilder().effectDateTime(value).build();
        actionManager.edit(parent, next);
    }
    @Override
    public MdTextFieldElement.MdOutlinedTextFieldElement element() {
        return ipt.element();
    }
    @AssistedFactory interface TypeEffectDateElementFactory {
        TypeEffectDateElement create(TypeElement parent);
    }
}
