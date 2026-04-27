package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.*;
import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.*;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.base.JsPropertyMap;

import javax.inject.Singleton;
import java.util.*;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public class MockModule {
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return behavior(Progress.hide()); }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static FetchApi fetchApi() { return new FetchApi() {}; }
    @Provides @Singleton static LanguageDetector languageDetector() { return () -> "en"; }
    @Provides @Singleton static LanguagePackRepository languagePackRepository() {
        return lang -> behavior(Labels.empty());
    }
    @Provides @Singleton static LabelProvider labelProvider() { 
        return new LabelProvider(() -> "en", lang -> behavior(Labels.empty()));
    }

    @Provides @Singleton static TypeRepository typeRepository() {
        return new TypeRepository() {
            @Override public Observable<Set<TypeValue>> list(LayoutPeriod period) { return BehaviorSubject.<Set<TypeValue>>behavior(new HashSet<>()).asObservable(); }
            @Override public Observable<Set<TypeValue>> save(Set<TypeValue> types) { return behavior(types).asObservable(); }
            @Override public Observable<Void> delete(Set<TypeValue> types) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
            @Override public Observable<Set<TypeValue>> patch(List<JsPropertyMap<?>> patches) { return BehaviorSubject.<Set<TypeValue>>behavior(new HashSet<>()).asObservable(); }
            @Override public Observable<Set<TypeValue>> versions(String typeId) { return BehaviorSubject.<Set<TypeValue>>behavior(new HashSet<>()).asObservable(); }
        };
    }
    @Provides @Singleton static LayoutRepository layoutRepository() {
        return new LayoutRepository() {
            @Override public Observable<List<LayoutPeriod>> layouts() { return BehaviorSubject.<List<LayoutPeriod>>behavior(new ArrayList<>()).asObservable(); }
            @Override public Observable<Map<String, Position>> positions(LayoutPeriod period) { return BehaviorSubject.<Map<String, Position>>behavior(new HashMap<>()).asObservable(); }
            @Override public Observable<Void> savePositions(LayoutPeriod period, Map<String, Position> positions) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
        };
    }
    @Provides @Singleton static TypeList typeList() { return new TypeList(); }
    @Provides @Singleton static PositionMap positionMap() { return new PositionMap(); }
    @Provides @Singleton static LayoutProvider layoutProvider() { return new LayoutProvider(); }
    @Provides @Singleton static LayoutList layoutList() { return new LayoutList(); }
    @Provides @Singleton static ActionManager actionManager() { return new ActionManager(); }
    @Provides @Singleton static ChangeTracker changeTracker() { return new ChangeTracker(); }
    @Provides @Singleton static MutationReceiver mutationReceiver() {
        return dev.sayaya.handbook.usecase.WindowMutationBridge.receiver();
    }
    @Provides @Singleton static ConfirmDialog confirmDialog() {
        return new ConfirmDialog();
    }
    @Provides @Singleton static ToastContainer toastContainer() {
        return new ToastContainer();
    }
}
