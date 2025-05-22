package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.interfaces.TypeTabsElement;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.api.DocumentApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.table.DocumentTableElement;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;
import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ClientSharedModule.class, ApiModule.class })
public interface Component {
    TypeTabsElement tabs();
    ControllerElement controller();
    DocumentTableElement table();
    TypeApi typeApi();
    DocumentApi documentApi();
    Observer<String> uri();
    Observer<Progress> progress();
    Observer<Render> renderer();
}