package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.DocumentApi;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.table.SpreadsheetElement;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.client.interfaces.table.PaginationElement;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { DocumentModule.class, MockApiModule.class })
public interface TestComponent {
    ControllerElement controller();
    SpreadsheetElement spreadsheetElement();
    DocumentApi documentApi();
    DocumentEventHandler documentEventHandler();
    AgentDocumentHandler agentDocumentHandler();
    DocumentStateProvider documentStateProvider();
    TypeList typeList();
    TypeProvider typeProvider();
    ToastContainer toastContainer();
    dev.sayaya.handbook.client.components.ConfirmDialog confirmDialog();
    PaginationElement pagination();
}
