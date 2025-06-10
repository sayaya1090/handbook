package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.Observable;
import elemental2.dom.DomGlobal;
import elemental2.dom.EventSource;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WorkspaceEventSource {
    @Delegate private EventSource source;
    @Inject WorkspaceEventSource(Observable<Workspace> workspace) {
        workspace.distinctUntilChanged().subscribe(w->{
            if(source!=null) source.close();
            if(w==null) source = null;
            else {
                source = new EventSource("workspace/" + w.id() + "/messages");
                source.onerror = err -> {
                    DomGlobal.console.error("err");
                    DomGlobal.console.error(err);
                };
            }
        });
    }
}