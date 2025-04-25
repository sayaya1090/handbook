package dev.sayaya.handbook.client.canvas;

import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { MockModule.class, ClientSharedModule.class })
public interface Component {
    ControllerElement controller();
    CanvasElement canvas();
    Observer<Workspace> workspaceProvider();
}
