package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.rx.subject.Subject;
import elemental2.dom.DomGlobal;
import elemental2.dom.MessageEvent;
import jsinterop.base.Js;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.Subject.subject;

@Singleton
public class UpdateDocumentEventSource {
    @Delegate private final Subject<String> subject = subject(String.class);
    @Inject UpdateDocumentEventSource(WorkspaceEventSource source) {
        source.addEventListener("UPDATE_DOCUMENT", evt-> {
            MessageEvent<String> cast = Js.cast(evt);
            DomGlobal.console.log("UPDATE_DOCUMENT", cast.data);
        });
    }
}
