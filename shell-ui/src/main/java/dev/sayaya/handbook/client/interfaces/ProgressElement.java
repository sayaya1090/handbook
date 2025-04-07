package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdProgressElement;
import dev.sayaya.ui.elements.ProgressElementBuilder;
import elemental2.dom.CSSProperties;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ProgressElement implements IsElement<MdProgressElement.MdLinearProgressElement> {
    @Delegate private final ProgressElementBuilder.LinearProgressElementBuilder _this = ProgressElementBuilder.progress().linear();
    @Inject ProgressElement(Observable<Progress> value) {
        value.subscribe(this::update);
    }
    private void update(Progress value) {
        if(!value.enabled()) {
            _this.element().style.opacity = CSSProperties.OpacityUnionType.of("0");
        } else {
            _this.element().style.opacity = CSSProperties.OpacityUnionType.of("100");
            _this.indeterminate(value.intermediate());
            _this.element().max = value.max();
            _this.value(value.value());
        }
    }
}
