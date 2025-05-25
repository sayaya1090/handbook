package dev.sayaya.handbook.client.interfaces.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Page<T> {
    private T[] content;
    private long totalPages;
    private long totalElements;
}
