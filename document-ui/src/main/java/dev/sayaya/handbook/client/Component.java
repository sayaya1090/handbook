package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.table.SpreadsheetElement;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.usecase.TypeRepository;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { DocumentModule.class, ApiModule.class })
public interface Component {
    ControllerElement controller();
    SpreadsheetElement spreadsheetElement();
    DocumentRepository documentRepository();
    DocumentEventHandler documentEventHandler();
    DocumentStateProvider documentStateProvider();
    TypeList typeList();
    TypeProvider typeProvider();
    ToastContainer toastContainer();
    }
