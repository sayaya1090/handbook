package dev.sayaya.handbook.client.interfaces.create;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.usecase.create.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.create.CreateWorkspaceParam;
import dev.sayaya.handbook.client.usecase.create.CreateWorkspaceState;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.elements.RadioElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder.OutlinedTextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import static dev.sayaya.ui.elements.RadioElementBuilder.radio;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class SectionElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> div = div().css("span");
    private final HTMLContainerBuilder<HTMLDivElement> container = div().style("""
            display: flex;
            flex-direction: column;
            margin-left: 1rem;
            gap: 1rem;
            width: 100%;
            """);
    private final RadioElementBuilder radio = radio().name("create-workspace");
    private final HTMLContainerBuilder<HTMLLabelElement> lbl = org.jboss.elemento.Elements.label().style("font-family: var(--md-sys-typescale-headline-large-font);");
    private final OutlinedTextFieldElementBuilder ipt = textField().outlined().css("text-field");
    private final CreateWorkspaceState state;
    private final CreateWorkspaceParam param;
    @AssistedInject SectionElement(@Assisted CreateWorkspaceState state, CreateWorkspaceMode mode, CreateWorkspaceParam param) {
        this.state = state;
        this.param = param;
        div.add(radio.value(state.toString()))
           .add(container.add(lbl).add(ipt));
        ipt.on(EventType.focus, evt->mode.next(state));
        radio.onChange(evt->ipt.element().focus());
        mode.subscribe(this::update);
        ipt.on(EventType.keydown, evt-> {
            if(mode.getValue()!=state) return;
            else Observable.timer(0, 10).take(1).subscribe(v->param.next(ipt.element().value));
        });
    }
    private void update(CreateWorkspaceState state) {
        if(this.state == state) radio.element().checked = true;
        else {
            radio.element().checked = false;
            ipt.element().value = "";
            param.next(ipt.element().value);
        }
    }
    public SectionElement label(String label) {
        lbl.element().innerHTML = label;
        return this;
    }
    public SectionElement supportingText(String value) {
        ipt.label(value);
        return this;
    }
}
