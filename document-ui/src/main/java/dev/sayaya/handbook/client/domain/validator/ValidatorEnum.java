package dev.sayaya.handbook.client.domain.validator;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder
public class ValidatorEnum implements ValidatorDefinition {
    private final String[] options;
}

