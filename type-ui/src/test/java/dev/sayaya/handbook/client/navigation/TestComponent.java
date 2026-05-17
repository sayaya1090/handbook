package dev.sayaya.handbook.client.navigation;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.controller.StatusHeaderElement;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { 
    dev.sayaya.handbook.client.TypeModule.class, 
    dev.sayaya.handbook.usecase.MockApiModule.class, 
    BoxElementModule.class,
    dev.sayaya.handbook.client.interfaces.editor.ValidatorEditorModule.class
})
public interface TestComponent {
    CanvasElement canvas();
    ControllerElement controller();
    StatusHeaderElement statusHeader();
    ActionManager actionManager();
    TypeList typeList();
    PositionMap positionMap();
    LayoutProvider layoutProvider();
    dev.sayaya.handbook.client.usecase.LayoutList layoutList();
    dev.sayaya.handbook.client.usecase.TypeDataCoordinator typeDataCoordinator();
    dev.sayaya.handbook.client.interfaces.api.TypeRepository typeRepository();
    dev.sayaya.handbook.client.interfaces.api.LayoutRepository layoutRepository();
    dev.sayaya.handbook.client.usecase.TypeToolManager typeToolManager();
}
