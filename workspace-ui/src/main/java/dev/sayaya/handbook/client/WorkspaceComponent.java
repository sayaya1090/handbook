package dev.sayaya.handbook.client;

import dagger.Component;
import dev.sayaya.handbook.client.interfaces.api.WorkspaceApiModule;
import dev.sayaya.handbook.client.usecase.WorkspaceApi;
import dev.sayaya.handbook.client.interfaces.GroupsTabElement;
import dev.sayaya.handbook.client.interfaces.InfoTabElement;
import dev.sayaya.handbook.client.interfaces.PermissionsTabElement;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        WorkspaceApiModule.class
})
public interface WorkspaceComponent {
    WorkspaceApi workspaceApi();
    InfoTabElement infoTab();
    GroupsTabElement groupsTab();
    PermissionsTabElement permissionsTab();
}
