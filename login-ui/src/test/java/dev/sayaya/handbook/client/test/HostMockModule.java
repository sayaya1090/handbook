package dev.sayaya.handbook.client.test;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.interfaces.api.FetchApi;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static elemental2.dom.DomGlobal.window;

@Module
public abstract class HostMockModule {
    static {
        ClientWindow.progress = behavior(new Progress());
        ClientWindow.uri = behavior(window.location.href);
        ClientWindow.renderer = behavior(null);
        ClientWindow.tools = behavior(new Tool[]{});
        ClientWindow.labels = behavior(null);
    }
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
}
