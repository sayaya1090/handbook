package dev.sayaya.handbook.client.usecase;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.api.FetchMock;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.rx.Observer;
import elemental2.dom.Response;

import javax.inject.Singleton;
import java.util.Map;

import static dev.sayaya.rx.subject.Subject.subject;

/**
 * 리다이렉트 테스트 전용 최소 의존성 Dagger 모듈.
 */
@Module
public interface RedirectModule {
    
    @Provides @Singleton static FetchMock provideFetchMock() {
        FetchMock mock = new FetchMock();
        
        // URL 에서 workspaces 파라미터 확인 (Scenario 2 시뮬레이션)
        String href = elemental2.dom.DomGlobal.window.location.href;
        boolean hasWorkspace = href != null && href.contains("workspaces=");
        
        if (hasWorkspace) {
            // Scenario 2: 워크스페이스 존재
            String body = "[{\"id\":\"ws-1\",\"name\":\"Workspace 1\"}]";
            Response mockResponse = FetchMock.createMockResponse(200, false, "http://127.0.0.1:18080/workspaces", body, Map.of());
            mock.when("workspaces", mockResponse);
        } else {
            // Scenario 1: 빈 워크스페이스 리다이렉트 (302 -> /onboarding)
            String finalUrl = "http://127.0.0.1:18080/workspaces/onboarding";
            Response mockResponse = FetchMock.createMockResponse(200, true, finalUrl, "[]", Map.of());
            mock.when("workspaces", mockResponse);
        }
        return mock; 
    }
    
    @Binds FetchApi provideFetchApi(FetchMock mock);
    @Binds WorkspaceRepository workspaceRepo(dev.sayaya.handbook.client.interfaces.api.WorkspaceApi impl);

    // 핵심 상태 관리자 (실제 인스턴스 필요)
    @Provides @Singleton static UriStore uriStore() { return new UriStore(); }
    @Provides @Singleton static dev.sayaya.rx.Observer<String> uriObserver(UriStore store) { return store; }
    @Provides @Singleton static WorkspaceList workspaceList(WorkspaceRepository repo) { return new WorkspaceList(repo); }
    @Provides @Singleton static SessionContext sessionContext() { return new SessionContext(); }
    @Provides @Singleton static PlaceholderResolver placeholderResolver(SessionContext context) { 
        return new PlaceholderResolver(context); 
    }

    // 보조 의존성 (최소화된 Mock)
    @Provides @Singleton static dev.sayaya.rx.Observer<dev.sayaya.handbook.domain.Progress> progress() { return dev.sayaya.rx.subject.Subject.subject(dev.sayaya.handbook.domain.Progress.class); }
    
    @Provides @Singleton static MenuSelected menuSelected() { 
        // HistoryManager 를 위해 빈 MenuSelected 를 생성 (생성자 public 확인됨)
        return new MenuSelected(null); 
    }
}
