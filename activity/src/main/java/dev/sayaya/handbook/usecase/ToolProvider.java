package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 도구 목록을 발행하고 선택 이벤트를 중계하는 공통 제공자.
 * 
 * <p>자식 모듈과 쉘 UI 간의 통신을 캡슐화하여 각 모듈에서 손쉽게 전역 도구를 사용할 수 있도록 한다.</p>
 */
@Singleton
public class ToolProvider {
    private final BehaviorSubject<Tool[]> tools;
    private final BehaviorSubject<String> selectedToolId;

    @Inject
    ToolProvider() {
        this.tools = behavior(new Tool[0]);
        this.selectedToolId = behavior(null);
    }

    // --- 자식 모듈(Child)용 API ---

    /** 현재 모듈의 도구 목록을 쉘에 알린다. */
    public void publish(Tool[] tools) {
        this.tools.next(tools);
        ToolPublisher.publish(tools);
    }

    /** 쉘에서 선택된 도구 이벤트를 구독한다. */
    public void onSelect(Consumer<String> callback) {
        ToolSubscriber.register(toolId -> {
            selectedToolId.next(toolId);
            callback.accept(toolId);
        });
    }

    // --- 쉘 UI(Host)용 API ---

    /** 자식 모듈이 발행하는 도구 목록을 수신한다. */
    public void subscribe(Consumer<Tool[]> callback) {
        ToolPublisher.register(tools -> {
            Tool[] casted = jsinterop.base.Js.cast(tools);
            this.tools.next(casted);
            callback.accept(casted);
        });
    }

    /** 사용자가 클릭한 도구를 자식 모듈에 알린다. */
    public void select(String toolId) {
        selectedToolId.next(toolId);
        ToolSubscriber.select(toolId);
    }

    public Observable<Tool[]> tools() { return tools.asObservable(); }
    public Observable<String> selectedToolId() { return selectedToolId.asObservable(); }
}
