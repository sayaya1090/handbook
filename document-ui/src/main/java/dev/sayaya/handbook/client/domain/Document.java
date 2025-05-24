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
    private Date createdDateTime;
    private String createdBy;
    @Singular("value")
    private Map<String, Object> values;
    @Builder.Default
    private DocumentState state = DocumentState.NOT_CHANGE;

    public enum DocumentState {
        NOT_CHANGE, CHANGE, DELETE
    }
}