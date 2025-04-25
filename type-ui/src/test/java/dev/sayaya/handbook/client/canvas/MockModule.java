package dev.sayaya.handbook.client.canvas;

import dagger.Binds;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.interfaces.LanguageRepository;
import dev.sayaya.handbook.client.interfaces.api.FetchApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.interfaces.box.BoxElementList;
import dev.sayaya.handbook.client.usecase.BoxTailor;
import dev.sayaya.handbook.client.usecase.LanguageProvider;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.handbook.client.usecase.UpdatableBoxList;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@dagger.Module
public abstract class MockModule {
    static {
        ClientWindow.progress = behavior(new Progress());
        ClientWindow.labels = behavior(null);
        ClientWindow.workspace = behavior(null);
    }
    @Provides @Singleton static BehaviorSubject<Label> labels() { return ClientWindow.labels; }
    @Provides @Singleton static Observer<Workspace> workspace() { return ClientWindow.workspace; }
    @Binds abstract LanguageProvider bindLanguageProvider(LanguageRepository impl);
    @Binds abstract UpdatableBoxList updatableBoxProvider(BoxElementList impl);
    @Provides static BoxTailor boxTailorProvider() {
        return box->{
            if(box == null) return 0;
            return 100 + box.values().size()*57;
        };
    }
    @Binds abstract TypeRepository typeRepositoryProvider(TypeApi impl);
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
}
