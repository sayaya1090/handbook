package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.HTMLLinkElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLElementBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.htmlElement;

@Singleton
public class FontElement implements IsElement<HTMLLinkElement> {
    @Delegate private final HTMLElementBuilder<HTMLLinkElement> _this = htmlElement("link", HTMLLinkElement.class).id("fonts").attr("rel", "stylesheet").attr("type", "text/css");
    @Inject
    FontElement(BehaviorSubject<Label> labels) {
        labels.subscribe(this::update);
    }
    private void update(Label label) {
       var url = label.fontUrl();
       _this.attr("href", url);
    }
}
