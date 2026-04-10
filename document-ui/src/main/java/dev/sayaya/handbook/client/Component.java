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
import dev.sayaya.handbook.client.usecase.TypeRepository;

import javax.inject.Singleton;

/**
 * document-ui 모듈의 Dagger 컴포넌트 인터페이스.
 *
 * <p><b>책임:</b> {@link DocumentModule}과 {@link ApiModule}에서 제공하는 바인딩을 조합하여
 * UI 컨트롤러, 스프레드시트, 에이전트 핸들러, API 클라이언트 등 최상위 객체 그래프를 구성한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentModule} — 뷰포트, 프로그레스, 토스트, 다국어 등 공통 바인딩</li>
 *   <li>{@link ApiModule} — FetchApi, DocumentRepository, TypeRepository 바인딩</li>
 * </ul></p>
 *
 * <p><b>주의:</b> Dagger가 컴파일 타임에 DaggerComponent 구현체를 생성한다.
 * Application 진입점에서 DaggerComponent.create()로 인스턴스를 얻어야 한다.</p>
 */
@Singleton
@dagger.Component(modules = { DocumentModule.class, ApiModule.class })
public interface Component {
    ControllerElement controller();
    SpreadsheetElement spreadsheet();
    AgentDocumentHandler agentHandler();
    DocumentEventHandler documentEventHandler();
    DocumentStateProvider documentStateProvider();
    DocumentApi documentApi();
    TypeApi typeApi();
    TypeList typeList();
    TypeProvider typeProvider();
    TypeRepository typeRepository();
    ToastContainer toastContainer();
}
