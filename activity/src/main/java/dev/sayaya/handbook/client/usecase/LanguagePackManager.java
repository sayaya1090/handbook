package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LanguagePackManager {
    private final LanguageProvider provider;
    private final Subject<Label> destination;
    @Inject LanguagePackManager(LanguageProvider provider, BehaviorSubject<Label> destination) {
        this.provider = provider;
        this.destination = destination;
    }
    public void initialize() {
        provider.getLanguage().distinct().subscribe(this::update);
    }
    private void update(String lang) {
        DomGlobal.fetch("js/language." + lang + ".json").then(response->{
            if(response.ok) response.json().then(json-> {
                Label label = Js.cast(json);
                destination.next(label);
                return null;
            }); else if(!"en".equalsIgnoreCase(lang)) update("en");
            return null;
        });
    }
}
