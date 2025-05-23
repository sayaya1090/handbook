package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Type;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
class BoxElementCache {
    private final Map<Type, BoxElement> cache = new ConcurrentHashMap<>();
    private final BoxElementFactory factory;
    @Inject BoxElementCache(BoxElementFactory factory) {
        this.factory = factory;
    }
    public BoxElement getOrCreate(Type box) {
        return cache.computeIfAbsent(box, factory::create);
    }
}
