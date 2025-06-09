package dev.sayaya.handbook.client.interfaces.api;

import elemental2.dom.DomGlobal;
import elemental2.dom.EventSource;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WorkspaceEventSource {
    @Delegate private final EventSource source;
    @Inject WorkspaceEventSource() {
        source = new EventSource("messages");
        source.onerror = err -> {
            DomGlobal.console.error("err");
            DomGlobal.console.error(err);
        };
    }
}


/*
@Provides @Singleton Subject<MrdEvent<Index>> updateSequencings(EventSource source) {
    var subject = subject(MrdEvent.class);
    source.addEventListener("UPDATE_SEQUENCING_INDEX", evt->{
        MessageEvent<String> cast = Js.cast(evt);
        Index param = (Index) JSON.parse(cast.data);
        var event = MrdEvent.builder().type("UPDATE_SEQUENCING_INDEX").param(param).build();
        subject.next(event);
    });
    return (Subject) subject;
}*/