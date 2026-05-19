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
import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.*;

@Module
public class MockApiModule {
    @Provides
    public FetchApi fetchApi() {
        return new FetchApi() {
            @Override public Promise<Response> request(String url) { return Promise.resolve(new Response()); }
            @Override public Promise<Response> request(String url, RequestInit init) { return Promise.resolve(new Response()); }
        };
    }

    private String periodKey(LayoutPeriod period) {
        String key = (long) Math.floor(period.effectDateTime()) + ":" + (long) Math.floor(period.expireDateTime());
        return key;
    }


    @Provides
    public TypeRepository typeRepository() {
        return new TypeRepository() {
            @Override
            public Observable<Set<Type>> list(LayoutPeriod period) {
                Set<Type> types = new HashSet<>();
                JsPropertyMap<Object> window = Js.asPropertyMap(DomGlobal.window);
                if (window.has("__mock_types")) {
                    JsPropertyMap<Type[]> mock = Js.cast(window.get("__mock_types"));
                    if (mock != null) {
                        String key = periodKey(period);
                        Type[] arr = mock.get(key);
                        if (arr != null) Collections.addAll(types, arr);
                    }
                }
                return BehaviorSubject.behavior(types).asObservable();
            }
            @Override public Observable<Set<Type>> save(Set<Type> types) { return BehaviorSubject.behavior(types).asObservable(); }
            @Override public Observable<Set<Type>> patch(List<JsPropertyMap<?>> patches) { return BehaviorSubject.<Set<Type>>behavior(new HashSet<>()).asObservable(); }
            @Override public Observable<Void> delete(Set<Type> types) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
            @Override public Observable<dev.sayaya.handbook.domain.SchemaPatch> patchSchema(dev.sayaya.handbook.domain.SchemaPatch patch) { return BehaviorSubject.behavior(patch).asObservable(); }
            @Override public Observable<Set<Type>> versions(String typeId) { return BehaviorSubject.<Set<Type>>behavior(new HashSet<>()).asObservable(); }
        };
    }

    @Provides
    public LayoutRepository layoutRepository() {
        return new LayoutRepository() {
            @Override
            public Observable<List<TypeLayout>> layouts() {
                List<TypeLayout> layouts = new ArrayList<>();
                JsPropertyMap<Object> window = Js.asPropertyMap(DomGlobal.window);
                if (window.has("__mock_layouts")) {
                    TypeLayout[] arr = Js.cast(window.get("__mock_layouts"));
                    if (arr != null) Collections.addAll(layouts, arr);
                }
                return BehaviorSubject.behavior(layouts).asObservable();
            }
            @Override
            public Observable<Map<String, Position>> positions(LayoutPeriod period) {
                Map<String, Position> positions = new HashMap<>();
                JsPropertyMap<Object> window = Js.asPropertyMap(DomGlobal.window);
                if (window.has("__mock_positions")) {
                    JsPropertyMap<JsPropertyMap<Position>> mock = Js.cast(window.get("__mock_positions"));
                    if (mock != null) {
                        String key = periodKey(period);
                        JsPropertyMap<Position> map = mock.get(key);
                        if (map != null) map.forEach(k -> positions.put(k, map.get(k)));
                    }
                }
                return BehaviorSubject.behavior(positions).asObservable();
            }
            @Override public Observable<Void> savePositions(LayoutPeriod period, Map<String, Position> positions) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
        };
    }

    @Provides
    public dev.sayaya.handbook.usecase.LanguagePackRepository languagePackRepository() {
        return locale -> BehaviorSubject.<Labels>behavior(Labels.empty()).asObservable();
    }
}
