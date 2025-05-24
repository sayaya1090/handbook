package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document doc = (Document) o;
        return Objects.equals(id, doc.id);
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public enum DocumentState {
        NOT_CHANGE, CHANGE, DELETE
    }
}