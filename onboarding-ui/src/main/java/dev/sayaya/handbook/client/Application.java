package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.RenderSharing;

/**
 * onboarding-ui 엔트리포인트.
 *
 * <p>Shell 의 FrameUpdater 는 {@link RenderSharing} 로 들어온 Render 를 받아
 * 새 Frame 엘리먼트를 생성·배치한 뒤 onInvoke(frameEl) 로 모듈이 frame 내부를 그릴 기회를 준다.
 * body 에 직접 append 하면 body(position:fixed inset:0) 기준으로 이미 100dvh 를 점유하는
 * shell 의 #content 바로 아래에 스택되어 뷰포트 밖으로 밀려 보이지 않는다 — 반드시 Render
 * 브릿지를 경유한다 (login-ui 와 동일 패턴).</p>
 */
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        
        // UC-W3/W4: 에이전트 워크스페이스 핸들러 초기화 (생성자에서 구독 등록)
        component.agentWorkspaceHandler();
        
        Render render = frame -> {
            frame.append(component.contentElement().element());
            return true;
        };
        RenderSharing.next(render);
    }
}
