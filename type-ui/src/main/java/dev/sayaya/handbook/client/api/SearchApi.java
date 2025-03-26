package dev.sayaya.handbook.client.api;

import dev.sayaya.handbook.client.domain.Page;
import dev.sayaya.handbook.client.domain.Search;
import elemental2.dom.Response;
import elemental2.dom.URLSearchParams;
import elemental2.promise.Promise;

public interface SearchApi<T> {
    Promise<Response> searchRequest(String url);
    default Promise<Page<T>> search(String url, Search search) {
        var param = searchParams(search);
        return searchRequest(url + "?" + param)
                .then(this::handleResponse)
                .then(this::parsePage)
                .catch_(this::handleException);
    }
    private URLSearchParams searchParams(Search search) {
        var params = new URLSearchParams();
        if(search!=null) {
            for(var query: search.filters().entrySet()) {
                if(query.getValue()!=null) params.append(query.getKey(), query.getValue());
                else params.append(query.getKey(), "");
            }
            params.set("page", String.valueOf(search.page()));
            params.set("limit", String.valueOf(search.limit()));
            if (search.asc() != null) params.set("asc", String.valueOf(search.asc()));
            if (search.sortBy() != null && !search.sortBy().isBlank()) params.set("sort_by", search.sortBy());
        }
        return params;
    }
    private Promise<Response> handleResponse(Response response) {
        return switch (response.status) {
            case 200 -> Promise.resolve(response);
            case 204 -> Promise.reject("Empty result");
            default  -> Promise.reject("HTTP Error: " + response.status + " - " + response.statusText);
        };
    }
    @SuppressWarnings("unchecked")
    private Promise<Page<T>> parsePage(Response response) {
        try {
            var totalPages = Long.parseLong(response.headers.get("Total-Pages"));
            var totalElements = Long.parseLong(response.headers.get("Total-Count"));
            return response.json().then(values-> Promise.resolve(Page.<T>builder()
                    .content((T[]) values)
                    .totalPages(totalPages)
                    .totalElements(totalElements)
                    .build()));
        } catch (NumberFormatException e) {
            return Promise.reject("Invalid response format in headers: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing response: " + e.getMessage());
        }
    }

    private <V> V handleException(Object throwable) {
        throw new RuntimeException("Search request failed: " + throwable);
    }
}
