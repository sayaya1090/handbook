package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        var origin = cache.get(key(type));
        return !type.equals(origin);
    }
}
