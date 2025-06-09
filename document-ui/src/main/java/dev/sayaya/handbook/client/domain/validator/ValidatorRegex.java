package dev.sayaya.handbook.client.domain.validator;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder
public class ValidatorRegex implements ValidatorDefinition {
    private final String pattern;
}

