package dev.sayaya.handbook.client.interfaces.api;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(fluent = true)
public class Search {
    private final int page;
    private final int limit;
    private final String sortBy;
    private final boolean asc;
    private final Map<String, String> filters;
    private Search(int page, int limit, String sortBy, boolean asc, Map<String, String> filters) {
        this.page = page;
        this.limit = limit;
        this.sortBy = sortBy;
        this.asc = asc;
        this.filters = Collections.unmodifiableMap(filters);
    }
    public static SearchBuilder builder() {
        return new SearchBuilder();
    }
    public static SearchBuilder from(Search search) {
        var builder = new SearchBuilder().page(search.page())
                .limit(search.limit())
                .sortBy(search.sortBy())
                .asc(search.asc());
        for(var filter: search.filters().entrySet()) builder.filter(filter.getKey(), filter.getValue());
        return builder;
    }
    @Setter
    public static class SearchBuilder {
        private int page = 0;
        private int limit = 30;
        private String sortBy = null;
        private boolean asc = false;
        private final Map<String, String> filters = new HashMap<>();
        private static final List<String> INVALID_KEYS = List.of("page", "limit", "sort_by", "asc");
        public SearchBuilder filter(String key, String value) {
            if (INVALID_KEYS.contains(key)) throw new IllegalArgumentException("'" + key + "' cannot be used as a filter key");
            filters.put(key, value);
            return this;
        }
        public SearchBuilder filter(String key) {
            return filter(key, null);
        }
        public Search build() {
            return new Search(page, limit, sortBy, asc, filters);
        }
    }
}