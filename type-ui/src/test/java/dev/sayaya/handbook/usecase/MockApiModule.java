package dev.sayaya.handbook.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.api.FetchApi;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.base.JsPropertyMap;

import javax.inject.Singleton;
import java.util.*;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public class MockApiModule {
    @Provides @Singleton static FetchApi fetchApi() { 
        return new FetchApi() {
            @Override public elemental2.promise.Promise<elemental2.dom.Response> request(String url) { 
                return elemental2.promise.Promise.resolve(new elemental2.dom.Response()); 
            }
            @Override public elemental2.promise.Promise<elemental2.dom.Response> request(String url, elemental2.dom.RequestInit init) { 
                return elemental2.promise.Promise.resolve(new elemental2.dom.Response()); 
            }
        };
    }
    @Provides @Singleton static LanguagePackRepository languagePackRepository() {
        return lang -> behavior(Labels.empty());
    }

    @Provides @Singleton static TypeRepository typeRepository() {
        return new TypeRepository() {
            @Override public Observable<Set<Type>> list(LayoutPeriod period) { return BehaviorSubject.<Set<Type>>behavior(new HashSet<>()).asObservable(); }
            @Override public Observable<Set<Type>> save(Set<Type> types) { return behavior(types).asObservable(); }
            @Override public Observable<Void> delete(Set<Type> types) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
            @Override public Observable<Set<Type>> patch(List<JsPropertyMap<?>> patches) { return BehaviorSubject.<Set<Type>>behavior(new HashSet<>()).asObservable(); }
            @Override public Observable<Set<Type>> versions(String typeId) { return BehaviorSubject.<Set<Type>>behavior(new HashSet<>()).asObservable(); }
        };
    }
    @Provides @Singleton static LayoutRepository layoutRepository() {
        return new LayoutRepository() {
            @Override public Observable<List<LayoutPeriod>> layouts() { return BehaviorSubject.<List<LayoutPeriod>>behavior(new ArrayList<>()).asObservable(); }
            @Override public Observable<Map<String, Position>> positions(LayoutPeriod period) { return BehaviorSubject.<Map<String, Position>>behavior(new HashMap<>()).asObservable(); }
            @Override public Observable<Void> savePositions(LayoutPeriod period, Map<String, Position> positions) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
        };
    }
}
