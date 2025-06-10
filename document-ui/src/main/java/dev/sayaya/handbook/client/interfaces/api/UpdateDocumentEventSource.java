package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.HandbookEvent;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.DomGlobal;
import elemental2.dom.MessageEvent;
import jsinterop.base.Js;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.Subject.subject;
import static elemental2.core.Global.JSON;

@Singleton
public class UpdateDocumentEventSource {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Delegate private final Subject<HandbookEvent<Document>> subject = (Subject) subject(HandbookEvent.class);
    @Inject UpdateDocumentEventSource(WorkspaceEventSource source) {
        source.addEventListener("UPDATE_DOCUMENT", evt-> {
            MessageEvent<String> cast = Js.cast(evt);
            DocumentNative param = (DocumentNative) JSON.parse(cast.data);
            var event = HandbookEvent.<Document>builder().type("UPDATE_DOCUMENT").param(param.toDomain()).build();
            subject.next(event);
        });
        subject.subscribe(evt-> DomGlobal.console.log(evt));
    }
}
