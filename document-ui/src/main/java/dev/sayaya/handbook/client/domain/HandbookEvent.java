package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent=true)
@Builder
public class HandbookEvent<T> {
    private String id;
    private String type;
    private T param;
}
