package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

@Data
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class Document {
    private String id;
    private String type;
    private String serial;
    private Date effectDateTime;
    private Date expireDateTime;
    @Singular("value")
    private Map<String, Object> values;
}