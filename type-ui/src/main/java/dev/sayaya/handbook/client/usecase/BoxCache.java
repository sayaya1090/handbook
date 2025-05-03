package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Period;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class BoxCache {
    private final Map<Period, List<Box>> cache = new HashMap<>();
    private final LayoutProvider layoutProvider;
    private final TypeRepository repository;
    private final BoxList boxList;
    @Inject BoxCache(LayoutProvider layoutProvider, TypeRepository repository, BoxList boxList) {
        this.layoutProvider = layoutProvider;
        this.repository = repository;
        this.boxList = boxList;
    }
    public void initialize() {
        layoutProvider.subscribe(this::update);
    }
    void update(Period period) {
        DomGlobal.console.log("cache update: "+period);
        if(period==null) return;
        else if(!cache.containsKey(period)) repository.list(period).subscribe(types->{
            this.cache.put(period, types);
            boxList.next(types.stream().toArray(Box[]::new));
        }); else boxList.next(cache.get(period).stream().toArray(Box[]::new));
    }
}
