package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.api.DocumentApi;
import dev.sayaya.handbook.client.interfaces.api.ProductionApiModule;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.table.PaginationElement;
import dev.sayaya.handbook.client.interfaces.table.SpreadsheetElement;
import dev.sayaya.handbook.client.usecase.*;

import javax.inject.Singleton;

/**
 * 운영 환경을 위한 메인 컴포넌트.
 * 모든 레이어의 모듈을 통합하여 어플리케이션에 필요한 싱글톤 인스턴스들을 관리한다.
 */
@Singleton
@dagger.Component(modules = { DocumentModule.class, ProductionApiModule.class })
public interface Component {
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
