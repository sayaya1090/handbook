package dev.sayaya.handbook.client.tab;

import com.google.gwt.i18n.client.DateTimeFormat;
import dagger.Binds;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.client.domain.validator.*;
import dev.sayaya.handbook.client.interfaces.LanguageRepository;
import dev.sayaya.handbook.client.interfaces.api.DocumentNative;
import dev.sayaya.handbook.client.usecase.DocumentRepository;
import dev.sayaya.handbook.client.usecase.LanguageProvider;
import dev.sayaya.handbook.client.usecase.TypeRepository;
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
import static dev.sayaya.handbook.client.domain.AttributeTypeDefinition.AttributeType.Array;
import static dev.sayaya.handbook.client.domain.AttributeTypeDefinition.AttributeType.Value;
import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static elemental2.core.Global.JSON;

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
                                    Attribute.builder().id("type_1$$$t1-v1$$$0").name("attr_1").type(AttributeTypeDefinition.builder().baseType(Value)
                                                    .validator(ValidatorRegex.builder()
                                                            .pattern("^[a-zA-Z0-9]+$")
                                                            .build())
                                            .build()).nullable(false).inherited(false).build(),
                                    Attribute.builder().id("type_1$$$t1-v1$$$4").name("attr_4").type(AttributeTypeDefinition.builder().baseType(Value)
                                            .validator(ValidatorBool.builder().build())
                                            .build()).nullable(false).inherited(false).build(),
                                    Attribute.builder().id("type_1$$$t1-v1$$$5").name("attr_5").type(AttributeTypeDefinition.builder().baseType(Value)
                                            .validator(ValidatorNumber.builder()
                                                    .min(0.0).max(100.0)
                                                    .build())
                                            .build()).nullable(false).inherited(false).build(),
                                    Attribute.builder().id("type_1$$$t1-v1$$$6").name("attr_6").type(AttributeTypeDefinition.builder().baseType(Value)
                                            .validator(ValidatorDate.builder().build())
                                            .build()).nullable(false).inherited(false).build(),
                                    Attribute.builder().id("type_1$$$t1-v1$$$7").name("attr_7").type(AttributeTypeDefinition.builder().baseType(Value)
                                            .validator(ValidatorEnum.builder().options(new String[] {
                                                    "Apple", "Banana", "Cherry"
                                            }).build())
                                            .build()).nullable(false).inherited(false).build(),
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
                            )).build(),
                    Type.builder()
                            .id("type_2")
                            .version("t2-v1")
                            .next("t2-v2")
                            .effectDateTime(dtf.parse("2025-01-01 00:00:00"))
                            .expireDateTime(dtf.parse("2025-08-01 00:00:00"))
                            .description("type_2")
                            .primitive(true)
                            .parent("type_1")
                            .attributes(List.of(
                                    Attribute.builder().id("type_1$$$t1-v1$$$0").name("attr_1").type(AttributeTypeDefinition.builder().baseType(Value).build()).nullable(false).inherited(false).build(),
                                    Attribute.builder().id("type_1$$$t1-v1$$$1").name("attr_2").type(AttributeTypeDefinition.builder().baseType(Array)
                                            .argument(AttributeTypeDefinition.builder().baseType(Value).build())
                                            .build()
                                    ).nullable(true).inherited(false).build())
                            ).build(),
                    Type.builder()
                            .id("type_2")
                            .version("t2-v2")
                            .prev("t2-v1")
                            .effectDateTime(dtf.parse("2025-08-01 00:00:00"))
                            .expireDateTime(dtf.parse("2025-12-31 00:00:00"))
                            .description("type_2")
                            .primitive(true)
                            .parent("type_1")
                            .attributes(List.of(
                                    Attribute.builder().id("type_1$$$t1-v1$$$0").name("attr_1").type(AttributeTypeDefinition.builder().baseType(Value).build()).nullable(false).inherited(false).build(),
                                    Attribute.builder().id("type_1$$$t1-v1$$$1").name("attr_2-2").type(AttributeTypeDefinition.builder().baseType(Array)
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
                                            ).nullable(false).inherited(true).build())
                            ).build(),
                    Type.builder()
                            .id("type_3")
                            .version("t3-v2")
                            .effectDateTime(dtf.parse("2025-09-01 00:00:00"))
                            .expireDateTime(dtf.parse("2025-12-31 00:00:00"))
                            .description("type_3")
                            .primitive(true)
                            .parent("type_2")
                            .attributes(Collections.emptyList())
                            .build()
            );
            @Override
            public Observable<List<Type>> list() {
                var filtered = types.stream().map(t->t.toBuilder().build()).collect(Collectors.toUnmodifiableList());
                return Observable.of(filtered);
            }
        };
    }
    @Provides static DocumentRepository documentRepositoryProvider() {
        return new DocumentRepository() {
            @Override
            public Observable<Void> save(Set<Document> toUpsert) {
                print(toUpsert);
                for(var doc: toUpsert) {
                    var nt = DocumentNative.from(doc);
                    try {
                        String s = JSON.stringify(nt);
                        DomGlobal.console.log("save", s);
                        DomGlobal.console.log(s);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return Observable.of((Void)null);
            }
            @Override
            public Observable<Void> delete(Set<Document> toDelete) {
                DomGlobal.console.log("delete", toDelete);
                return Observable.of((Void)null);
            }
        };
    }
    private static void print(Set<Document> set) {
        StringBuilder sb = new StringBuilder().append("Map {");
        for(var e: set) sb.append(e).append(", ");
        DomGlobal.console.log(sb.append("}").toString());
    }
}
