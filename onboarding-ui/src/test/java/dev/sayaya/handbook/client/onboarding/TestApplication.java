package dev.sayaya.handbook.client.onboarding;

import com.google.gwt.core.client.EntryPoint;

import static org.jboss.elemento.Elements.body;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        TestComponent component = DaggerTestComponent.create();
        // UC-W3/W4: 에이전트 워크스페이스 핸들러 초기화 (생성자에서 구독 등록)
        component.agentWorkspaceHandler();
        body().add(component.contentElement());
    }
}
