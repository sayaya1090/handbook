package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/*
 * DB에서 가져온 Type을 캐싱한다.
 * Type의 ID로 캐싱하고, 저장 이벤트 발생 시 변경된 데이터를 순회하여
 *   - 삭제 플래그가 있고 캐시에 있으면 -> 삭제
 *   - 삭제 플래그가 없고 (캐시에 없거나 캐시값과 다르면) -> 저장
 */
@Singleton
public class RepositoryTypeCache {
    private final Map<String, Type> cache = new HashMap<>();
    private final TypeRepository repository;
    @Inject RepositoryTypeCache(TypeRepository repository) {
        this.repository = repository;
    }
    Observable<List<Type>> list(Period period) {
        return repository.list(period).map(types-> types.stream().peek(origin -> {
            var replica = origin.toBuilder().build();
            cache.put(key(replica), replica);
        }).collect(Collectors.toUnmodifiableList()));
    }
    private static String key(Type type) {
        return type.id() + "$$$" + type.version();
    }
    public boolean contains(Type type) {
        return cache.containsKey(key(type));
    }
    public boolean isChanged(Type type) {
        return !equalsExactly(type, cache.get(key(type)));
    }
    private boolean equalsExactly(Type type, Type origin) {
        if(!type.equals(origin)) return false;
        return Objects.equals(type.effectDateTime(), origin.effectDateTime()) &&
               Objects.equals(type.expireDateTime(), origin.expireDateTime()) &&
               Objects.equals(type.description(), origin.description()) &&
               Objects.equals(type.primitive(), origin.primitive()) &&
               equalsExactly(type.attributes(), origin.attributes()) &&
               Objects.equals(type.parent(), origin.parent()) &&
               type.x() == origin.x() &&
               type.y() == origin.y() &&
               type.width() == origin.width() &&
               type.height() == origin.height();
    }
    private boolean equalsExactly(List<Attribute> attributes, List<Attribute> origin) {
        if(attributes.size() != origin.size()) return false;
        for (int i = 0; i < attributes.size(); i++) if(!equalsExactly(attributes.get(i), origin.get(i))) return false;
        return true;
    }
    private boolean equalsExactly(Attribute attribute, Attribute origin) {
        return Objects.equals(attribute.name(), origin.name()) &&
               Objects.equals(attribute.type(), origin.type()) &&
               Objects.equals(attribute.keyType(), origin.keyType()) &&
               Objects.equals(attribute.valueType(), origin.valueType()) &&
               Objects.equals(attribute.parent(), origin.parent()) &&
               Objects.equals(attribute.description(), origin.description()) &&
               attribute.nullable() == origin.nullable() &&
               attribute.inherited() == origin.inherited();
    }
}
