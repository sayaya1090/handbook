package dev.sayaya.handbook.client.interfaces.api;

import elemental2.dom.Response;
import elemental2.dom.URLSearchParams;
import elemental2.promise.Promise;

public interface SearchApi<T> {
    default URLSearchParams searchParams(Search search) {
        var params = new URLSearchParams();
        if(search!=null) {
            for(var query: search.filters().entrySet()) {
                if(query.getValue()!=null) params.append(query.getKey(), query.getValue());
                else params.append(query.getKey(), "");
            }
            params.set("page", String.valueOf(search.page()));
            params.set("limit", String.valueOf(search.limit()));
            params.set("asc", String.valueOf(search.asc()));
            params.set("sort_by", search.sortBy());
        }
        return params;
    }
    Promise<Response> searchRequest(String url);
    default Promise<Page<T>> search(String url, Search search) {
        var param = searchParams(search);
        return searchRequest(url + "?" + param)
                .then(this::handleResponse)
                .then(this::parse);
    }
    private Promise<Response> handleResponse(Response response) {
        return switch (response.status) {
            case 200 -> Promise.resolve(response);
            case 204 -> Promise.reject("Empty result");
            default  -> Promise.reject("HTTP Error: " + response.status + " - " + response.statusText);
        };
    }
    @SuppressWarnings("unchecked")
    private Promise<Page<T>> parse(Response response) {
        String totalPages = response.headers.get("X-Total-Pages");
        String totalElements = response.headers.get("X-Total-Count");
        return response.json().then(values-> Promise.resolve(new Page<>(
                (T[])values,
                totalPages!=null?Long.parseLong(totalPages):0,
                totalElements!=null?Long.parseLong(totalElements):0
        )));
    }
}
