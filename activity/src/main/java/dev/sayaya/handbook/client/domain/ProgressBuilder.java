package dev.sayaya.handbook.client.domain;

import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true)
public class ProgressBuilder {
    private boolean enabled = true;
    private boolean intermediate = false;
    private Double value = 0.0;
    private Double max = 1.0;
    public Progress build() {
        return Progress.of(enabled, intermediate, value, max);
    }
}
