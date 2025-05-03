package dev.sayaya.handbook.client.domain;

import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true)
public class WorkspaceBuilder {
    private String id;
    private String name;
    public Workspace build() {
        return Workspace.of(id, name);
    }
}
