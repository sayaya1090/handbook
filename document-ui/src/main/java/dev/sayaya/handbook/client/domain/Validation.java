package dev.sayaya.handbook.client.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class Validation {
    @Singular("value")
    private Map<String, Boolean> values;
    @Builder.Default
    private ValidationState state = ValidationState.DONE;

    public enum ValidationState {
        DONE
    }
}