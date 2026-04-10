package dev.sayaya.handbook.client.canvas;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.client.usecase.LayoutRepository;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.MutationReceiver;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

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
    @Provides @Singleton static TypeRepository typeRepository() {
        return new TypeRepository() {
            @Override public Observable<Set<TypeValue>> list(LayoutPeriod period) { return BehaviorSubject.<Set<TypeValue>>behavior(Collections.emptySet()).asObservable(); }
            @Override public Observable<Set<TypeValue>> save(Set<TypeValue> types) { return BehaviorSubject.<Set<TypeValue>>behavior(types).asObservable(); }
            @Override public Observable<Void> delete(Set<TypeValue> types) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
        };
    }
    @Provides @Singleton static LayoutRepository layoutRepository() {
        return new LayoutRepository() {
            @Override public Observable<List<LayoutPeriod>> layouts() { return BehaviorSubject.<List<LayoutPeriod>>behavior(Collections.emptyList()).asObservable(); }
            @Override public Observable<Map<String, Position>> positions(LayoutPeriod period) { return BehaviorSubject.<Map<String, Position>>behavior(Collections.emptyMap()).asObservable(); }
            @Override public Observable<Void> savePositions(LayoutPeriod period, Map<String, Position> positions) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
        };
    }
    @Provides @Singleton static MutationReceiver mutationReceiver() {
        return dev.sayaya.handbook.usecase.WindowMutationBridge.receiver();
    }
}
