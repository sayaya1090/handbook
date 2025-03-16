package dev.sayaya.handbook.client.api;

import lombok.Builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record Search(
        int page,
        int limit,
        String sortBy,
        Boolean asc,
        Map<String, String> filters
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 100;
    private static final List<String> INVALID_KEYS = List.of("page", "limit", "sort_by", "asc");
    public Search {
        require(page >= 0, "Page must be a non-negative integer. Given: " + page);
        require(limit >= 1 && limit <= MAX_LIMIT, "Limit must be greater than 0, and less than or equal to " + MAX_LIMIT + ". Given: " + limit);
        require(asc == null || sortBy != null, "If 'asc' is not null, 'sortBy' must also be provided. Given: asc=" + asc + ", sortBy=" + sortBy);
        if (filters == null) filters = Collections.emptyMap();
        else {
            Map<String, String> validatedFilters = new HashMap<>();
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                validateFilterKey(entry.getKey());
                validatedFilters.put(entry.getKey(), entry.getValue());
            }
            filters = Collections.unmodifiableMap(validatedFilters);
        }
    }
    public Search withFilter(String key, String value) {
        validateFilterKey(key);
        Map<String, String> newFilters = new HashMap<>(this.filters);
        newFilters.put(key, value);
        return this.toBuilder().filters(Collections.unmodifiableMap(newFilters)).build();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    // 필터 키 검증 로직
    private static void validateFilterKey(String key) {
        require(!INVALID_KEYS.contains(key), "'" + key + "' cannot be used as a filter key");
    }
    public static Search.SearchBuilder builder() {
        return new SearchBuilder()
                .page(DEFAULT_PAGE)
                .limit(DEFAULT_LIMIT)
                .filters(Collections.emptyMap());
    }
}
