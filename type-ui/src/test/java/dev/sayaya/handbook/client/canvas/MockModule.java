package dev.sayaya.handbook.client.canvas;

import com.google.gwt.i18n.client.DateTimeFormat;
import dagger.Binds;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.client.interfaces.LanguageRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeNative;
import dev.sayaya.handbook.client.interfaces.box.BoxElementList;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.sayaya.handbook.client.domain.AttributeTypeDefinition.AttributeType.*;
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
            return 170 + box.attributes().size()*41;
        };
    }
    private static final DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
    @Provides static TypeRepository typeRepositoryProvider() {
        return new TypeRepository() {
            private final List<Type> types = List.of(
                    Type.builder()
                            .id("type_1")
                            .version("t1-v1")
                            .effectDateTime(dtf.parse("2025-01-01 18:00:00"))
                            .expireDateTime(dtf.parse("2025-12-31 00:00:00"))
                            .description("type_1")
                            .primitive(true)
                            .parent(null)
                            .attributes(List.of(
                                    Attribute.builder().id("type_1$$$t1-v1$$$0").name("attr_1").type(AttributeTypeDefinition.builder().baseType(Value).build()).nullable(false).inherited(false).build(),
                                    Attribute.builder().id("type_1$$$t1-v1$$$1").name("attr_2").type(AttributeTypeDefinition.builder().baseType(Array)
                                                    .argument(AttributeTypeDefinition.builder().baseType(Value).build())
                                            .build()
                                    ).nullable(true).inherited(false).build(),
                                    Attribute.builder().id("type_1$$$t1-v1$$$2").name("attr_3")
                                            .type(AttributeTypeDefinition.builder().baseType(Map)
                                                    .argument(AttributeTypeDefinition.builder().baseType(Value).build())
                                                    .argument(AttributeTypeDefinition.builder().baseType(Array)
                                                            .argument(AttributeTypeDefinition.builder().baseType(Value).build())
                                                            .build())
                                                    .build()
                                            ).nullable(false).inherited(true).build()
                            )).x(100).y(100).width(300).height(293).build(),
                    Type.builder()
                            .id("type_2")
                            .version("t2-v1")
                            .effectDateTime(dtf.parse("2025-03-01 00:00:00"))
                            .expireDateTime(dtf.parse("2025-08-01 00:00:00"))
                            .description("type_2")
                            .primitive(true)
                            .parent("type_1")
                            .attributes(Collections.emptyList())
                            .x(700).y(100).width(300).height(170).build(),
                    Type.builder()
                            .id("type_2")
                            .version("t2-v2")
                            .effectDateTime(dtf.parse("2025-08-01 00:00:00"))
                            .expireDateTime(dtf.parse("2025-12-31 00:00:00"))
                            .description("type_2")
                            .primitive(true)
                            .parent("type_1")
                            .attributes(Collections.emptyList())
                            .x(700).y(100).width(300).height(170).build(),
                    Type.builder()
                            .id("type_3")
                            .version("t3-v1")
                            .effectDateTime(dtf.parse("2025-03-01 00:00:00"))
                            .expireDateTime(dtf.parse("2025-09-01 00:00:00"))
                            .description("type_1")
                            .primitive(true)
                            .parent("type_2")
                            .attributes(Collections.emptyList())
                            .x(200).y(500).width(300).height(170).build(),
                    Type.builder()
                            .id("type_3")
                            .version("t3-v2")
                            .effectDateTime(dtf.parse("2025-09-01 00:00:00"))
                            .expireDateTime(dtf.parse("2025-12-31 00:00:00"))
                            .description("type_3")
                            .primitive(true)
                            .parent("type_2")
                            .attributes(Collections.emptyList())
                            .x(200).y(500).width(300).height(170).build()
            );
            @Override
            public Observable<List<Type>> list(Period period) {
                long periodStart = period.effectDateTime().getTime();
                long periodEnd = period.expireDateTime().getTime();
                var filtered = types.stream().filter(type -> {
                    long typeStart = type.effectDateTime().getTime();
                    long typeEnd = type.expireDateTime().getTime();
                    return (typeStart < periodEnd) && (periodStart < typeEnd);
                }).map(t->t.toBuilder().build()).collect(Collectors.toUnmodifiableList());
                return Observable.of(filtered);
            }

            @Override
            public Observable<Void> save(Set<Type> toDelete, Set<Type> toUpsert) {
                var natives = Stream.concat(
                        toDelete.stream().map(type-> TypeNative.from(type, true)),
                        toUpsert.stream().map(type->TypeNative.from(type, false))
                ).toArray(TypeNative[]::new);
                DomGlobal.console.log("save", natives);
                return Observable.of((Void)null);
            }
        };
    }
    @Provides static LayoutRepository layout() {
        return new LayoutRepository() {
            private final List<Period> periods = List.of(
                    Period.builder().effectDateTime(dtf.parse("2025-01-01 18:00:00")).expireDateTime(dtf.parse("2025-03-01 00:00:00")).build(),
                    Period.builder().effectDateTime(dtf.parse("2025-03-01 00:00:00")).expireDateTime(dtf.parse("2025-08-01 00:00:00")).build(),
                    Period.builder().effectDateTime(dtf.parse("2025-08-01 00:00:00")).expireDateTime(dtf.parse("2025-09-01 00:00:00")).build(),
                    Period.builder().effectDateTime(dtf.parse("2025-09-01 00:00:00")).expireDateTime(dtf.parse("2025-12-31 00:00:00")).build()
            );
            @Override
            public Observable<List<Period>> layouts() {
                return Observable.of(periods);
            }
        };
    }
}
