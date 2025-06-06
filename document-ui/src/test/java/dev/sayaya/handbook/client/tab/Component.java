package dev.sayaya.handbook.client.tab;

import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.interfaces.TypeTabsElement;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.table.DocumentTableElement;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { MockModule.class, ClientSharedModule.class })
public interface Component {
    TypeTabsElement tabs();
    ControllerElement controller();
    DocumentTableElement table();
    TypeRepository typeRepository();
    TypeList typeList();
    DocumentList documentList();
    Observer<Workspace> workspaceProvider();
}
