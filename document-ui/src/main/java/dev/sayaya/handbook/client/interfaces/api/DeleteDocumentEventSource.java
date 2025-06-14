package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.HandbookEvent;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.MessageEvent;
import jsinterop.base.Js;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.Subject.subject;
import static elemental2.core.Global.JSON;

@Singleton
public class DeleteDocumentEventSource {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Delegate private final Subject<HandbookEvent<Document>> subject = (Subject) subject(HandbookEvent.class);
    @Inject DeleteDocumentEventSource(WorkspaceEventSource source) {
        source.addEventListener("DELETE_DOCUMENT", evt-> {
            MessageEvent<String> cast = Js.cast(evt);
            DocumentNative param = (DocumentNative) JSON.parse(cast.data);
            var event = HandbookEvent.<Document>builder().type("DELETE_DOCUMENT").param(param.toDomain()).build();
            subject.next(event);
        });
    }
}
