package dev.sayaya.handbook.client.domain.validator;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder
public class ValidatorDate implements ValidatorDefinition {
    private final Double min;
    private final Double max;
}

