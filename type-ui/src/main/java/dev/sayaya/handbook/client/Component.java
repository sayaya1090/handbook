package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.editor.AttributeEditorDialog;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { TypeModule.class, ApiModule.class, BoxElementModule.class })
public interface Component {
    CanvasElement canvas();
    ControllerElement controller();
    AttributeEditorDialog attributeEditor();
    dev.sayaya.handbook.client.usecase.TypeStateProvider typeStateProvider();
    dev.sayaya.handbook.client.usecase.TypeSearchProvider typeSearchProvider();
    dev.sayaya.handbook.client.usecase.TypeEventHandler typeEventHandler();
    dev.sayaya.handbook.client.components.ToastContainer toastContainer();
}
