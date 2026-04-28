package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.table.SpreadsheetElement;
import dev.sayaya.handbook.client.usecase.AgentDocumentHandler;
import dev.sayaya.handbook.client.usecase.DocumentEventHandler;
import dev.sayaya.handbook.client.usecase.DocumentStateProvider;
import dev.sayaya.handbook.client.interfaces.api.DocumentApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.handbook.usecase.TypeRepository;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { DocumentModule.class, ApiModule.class })
public interface Component {
    ControllerElement controller();
    SpreadsheetElement spreadsheetElement();
    AgentDocumentHandler agentDocumentHandler();
    DocumentEventHandler documentEventHandler();
    DocumentStateProvider documentStateProvider();
    TypeList typeList();
    TypeProvider typeProvider();
    TypeRepository typeRepository();
    ToastContainer toastContainer();
}
