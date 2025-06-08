package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.validator.ValidatorSelect;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import java.util.LinkedList;
import java.util.List;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.IconElementBuilder.icon;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

public class ValidatorSelectEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div().style("""
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 0.5rem;
        """);
    private final LinkedList<Option> options = new LinkedList<>();
    private final BehaviorSubject<AttributeTypeDefinition> subject;
    @AssistedInject ValidatorSelectEditorElement(@Assisted BehaviorSubject<AttributeTypeDefinition> subject, @Assisted ValidatorSelect validator) {
        this.subject = subject;
        var option = new Option(true);
        if(validator.options()!=null && validator.options().length > 0) option.select.value(validator.options()[0]);
        options.add(option);
        container.add(option);
        if(validator.options()!=null) for(int i = 1; i < validator.options().length; i++) {
            var value = validator.options()[i];
            append(value);
        }
    }
    private void append(String initial) {
        var option = new Option(false);
        option.select.value(initial);
        options.add(option);
        container.add(option);
    }
    private AttributeTypeDefinition build() {
        var options = this.options.stream()
                .map(o->o.select.value())
                .filter(s->!s.isEmpty())
                .toArray(String[]::new);
        return AttributeTypeDefinition.builder()
                .baseType(AttributeTypeDefinition.AttributeType.Value)
                .clearValidators()
                .validators(List.of(ValidatorSelect.builder().options(options).build()))
                .build();
    }
    @AssistedFactory
    interface ValidatorSelectEditorElementFactory {
        ValidatorSelectEditorElement select(BehaviorSubject<AttributeTypeDefinition> subject, ValidatorSelect validator);
    }
    private final class Option implements IsElement<HTMLElement> {
        @Delegate private final HTMLContainerBuilder<HTMLElement> container = span().style("""
            display: flex;
            align-items: center;
            gap: 0.5rem;
        """);
        private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder select = textField().outlined().label("Option");
        Option(boolean isFirst) {
            add(select);
            if (isFirst) add(btnAdd());
            else add(btnRemove());
            select.onChange(evt->{
               var def = build();
               subject.next(def);
            });
        }

        private IconButtonElementBuilder.PlainIconButtonElementBuilder btnAdd() {
            var btn = button().icon().add(icon("add"));
            btn.onClick(evt-> {
                evt.preventDefault();
                append("");
            });
            return btn;
        }
        private IconButtonElementBuilder.PlainIconButtonElementBuilder btnRemove() {
            var btn = button().icon().add(icon("remove"));
            btn.onClick(evt-> {
                evt.preventDefault();
                Option option = this;
                option.element().remove();
                options.remove(option);

                var def = build();
                subject.next(def);
            });
            return btn;
        }
    }
}