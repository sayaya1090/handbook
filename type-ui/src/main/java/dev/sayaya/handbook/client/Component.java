package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.interfaces.controller.ActionDialElement;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.controller.SettingsDialElement;
import dev.sayaya.handbook.client.interfaces.controller.StatusHeaderElement;
import dev.sayaya.handbook.client.interfaces.editor.AttributeEditorDialog;
import dev.sayaya.handbook.client.interfaces.editor.DateCorrectionDialog;
import dev.sayaya.handbook.client.interfaces.editor.VersionCreationDialog;
import dev.sayaya.handbook.client.usecase.*;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { TypeModule.class, ApiModule.class, BoxElementModule.class })
public interface Component {
    CanvasElement canvas();
    ControllerElement controller();
    StatusHeaderElement statusHeader();
    ActionDialElement actionDial();
    SettingsDialElement settingsDial();
    AttributeEditorDialog attributeEditor();
    DateCorrectionDialog dateCorrectionDialog();
    VersionCreationDialog versionCreationDialog();
    TypeToolManager typeToolManager();
    TypeList typeList();
    PositionMap positionMap();
    TypeStateProvider typeStateProvider();
    TypeSearchProvider typeSearchProvider();
    TypeEventHandler typeEventHandler();
    ToastContainer toastContainer();
    TypeRepository typeRepository();
    LayoutRepository layoutRepository();
    ActionManager actionManager();
    ChangeTracker changeTracker();
    LayoutProvider layoutProvider();
    LayoutList layoutList();
}
