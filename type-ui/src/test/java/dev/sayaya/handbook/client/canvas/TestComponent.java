package dev.sayaya.handbook.client.canvas;

import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.editor.AttributeEditorDialog;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.AgentMutationHandler;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { MockModule.class, BoxElementModule.class })
public interface TestComponent {
    CanvasElement canvas();
    ControllerElement controller();
    AttributeEditorDialog attributeEditor();
    ActionManager actionManager();
    TypeList typeList();
    PositionMap positionMap();
    LayoutProvider layoutProvider();
    AgentMutationHandler agentMutationHandler();
}
