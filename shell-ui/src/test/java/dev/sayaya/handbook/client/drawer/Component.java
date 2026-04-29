package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.ShellModule;
import dev.sayaya.handbook.client.interfaces.drawer.ShellAppBarElement;
import dev.sayaya.handbook.client.interfaces.drawer.WorkspaceSelectElement;
import dev.sayaya.handbook.client.usecase.WorkspaceList;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { Module.class })
public interface Component {
    void inject(Application app);
    ShellAppBarElement shellAppBar();
    WorkspaceSelectElement workspaceSelect();
    WorkspaceList workspaceList();
}
