package dev.sayaya.handbook.client.usecase.language;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.Subject.subject;

@Singleton
public class LanguagePackManager {
    @Delegate private final Subject<Label> labels = subject(Label.class);
    @Inject LanguagePackManager(LanguageProvider provider) {
        provider.getLanguage().distinct().subscribe(this::update);
    }
    private void update(String lang) {
        DomGlobal.fetch("js/language." + lang + ".json").then(response->{
            if(response.ok) response.json().then(json-> {
                Label label = Js.cast(json);
                labels.next(label);
                return null;
            }); else update("en");
            return null;
        });
    }
}
