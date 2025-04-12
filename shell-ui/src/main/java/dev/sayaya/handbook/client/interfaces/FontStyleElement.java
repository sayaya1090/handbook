package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.HTMLStyleElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLElementBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.htmlElement;

@Singleton
public class FontStyleElement implements IsElement<HTMLStyleElement> {
    @Delegate private final HTMLElementBuilder<HTMLStyleElement> _this = htmlElement("style", HTMLStyleElement.class).id("font-styles");
    @Inject FontStyleElement(BehaviorSubject<Label> labels) {
        labels.subscribe(this::update);
    }
    private final static String STYLE_DEF = """
        :root {
          --md-ref-typeface-plain: {0};
          --md-sys-typescale-headline-large-font: {1};
          --md-sys-typescale-headline-small-font: {2};
          --md-sys-typescale-label-large-font: {3};
          --md-sys-typescale-label-small-font: {4};
          --md-sys-typescale-body-large-font: {5};
          --md-sys-typescale-body-medium-font: {6};
        }
        """;
    private void update(Label label) {
        var mdRefTypefacePlain = label.mdRefTypefacePlain();
        var mdSysTypescaleHeadline = label.mdSysTypescaleHeadline();
        var mdSysTypescaleLabel = label.mdSysTypescaleLabel();
        var mdSysTypescaleBody = label.mdSysTypescaleBody();
        element().textContent = STYLE_DEF
                .replace("{0}", mdRefTypefacePlain)
                .replace("{1}", mdSysTypescaleHeadline)
                .replace("{2}", mdSysTypescaleHeadline)
                .replace("{3}", mdSysTypescaleLabel)
                .replace("{4}", mdSysTypescaleLabel)
                .replace("{5}", mdSysTypescaleBody)
                .replace("{6}", mdSysTypescaleBody);

    }
}
