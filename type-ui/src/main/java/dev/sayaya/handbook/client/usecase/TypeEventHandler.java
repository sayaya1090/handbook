package dev.sayaya.handbook.client.usecase;


import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.PresenceTracker;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.WorkspaceEventReceiver;
import elemental2.core.Global;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 워크스페이스 SSE 이벤트를 구독하여 타입 관련 이벤트를 처리한다.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>TYPE_CREATED/DELETED — 현재 레이아웃 기간의 타입 목록 재조회 + ChangeTracker/ActionManager 초기화 + 토스트</li>
 *   <li>PRESENCE — JSON 파싱 후 {@link PresenceTracker}에 편집 위치 전달</li>
 * </ul></p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link WorkspaceEventReceiver} — SSE 이벤트 스트림</li>
 *   <li>{@link TypeRepository} — 타입 재조회 (PATCH 지원)</li>
 *   <li>{@link ChangeTracker} — 더티 상태 초기화 (타입 갱신 시)</li>
 *   <li>{@link ActionManager} — Undo/Redo 스택 초기화</li>
 *   <li>{@link PresenceTracker} — 프레즌스 상태 관리</li>
 *   <li>{@link LabelProvider} — 토스트 메시지 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> {@link #init()}을 Application에서 호출해야 구독이 시작된다.
 * 타입 갱신 시 ChangeTracker와 ActionManager를 모두 초기화하므로 미저장 변경은 소실된다.</p>
 */
@Singleton
public class TypeEventHandler {
    private final WorkspaceEventReceiver eventReceiver;
    private final TypeRepository typeRepository;
    private final TypeList typeList;
    private final LayoutProvider layoutProvider;
    private final ChangeTracker tracker;
    private final ActionManager actionManager;
    private final ToastContainer toastContainer;
    private final PresenceTracker presenceTracker;
    private Labels labels = Labels.empty();

    @Inject
    public TypeEventHandler(WorkspaceEventReceiver eventReceiver,
                            TypeRepository typeRepository,
                            TypeList typeList,
                            LayoutProvider layoutProvider,
                            ChangeTracker tracker,
                            ActionManager actionManager,
                            ToastContainer toastContainer,
                            LabelProvider labelProvider,
                            PresenceTracker presenceTracker) {
        this.eventReceiver = eventReceiver;
        this.typeRepository = typeRepository;
        this.typeList = typeList;
        this.layoutProvider = layoutProvider;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.toastContainer = toastContainer;
        this.presenceTracker = presenceTracker;
        labelProvider.subscribe(l -> this.labels = l);
    }

    public void init() {
        eventReceiver.events().subscribe(this::handleEvent);
    }

    private void handleEvent(String eventData) {
        if (eventData == null) return;
        int colonIdx = eventData.indexOf(':');
        if (colonIdx < 0) return;
        String eventType = eventData.substring(0, colonIdx);

        switch (eventType) {
            case "TYPE_CREATED":
            case "TYPE_DELETED":
                refreshTypes();
                toastContainer.show(ToastLevel.INFO, labels.getOrDefault("type.event.changed", "Another user has modified the type"));
                break;
            case "PRESENCE":
                handlePresence(eventData.substring(colonIdx + 1));
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void handlePresence(String json) {
        GWT.log("TypeEventHandler: presence event: " + json);
        JsPropertyMap<Object> parsed = Js.cast(Global.JSON.parse(json));
        String user = Js.cast(parsed.get("user"));
        String userName = parsed.has("user_name") ? Js.cast(parsed.get("user_name")) : user;
        String type = parsed.has("type") ? Js.cast(parsed.get("type")) : null;
        String serial = parsed.has("serial") ? Js.cast(parsed.get("serial")) : null;
        String field = parsed.has("field") ? Js.cast(parsed.get("field")) : null;
        presenceTracker.update(user, userName, type, serial, field);
    }

    private void refreshTypes() {
        TypeLayout layout = layoutProvider.getValue();
        if (layout == null) return;
        GWT.log("TypeEventHandler: refreshing types for current layout period");
        typeRepository.list(layout.toPeriod()).subscribe(types -> {
            if (types != null) {
                typeList.replace(types);
                tracker.reset();
                actionManager.clear();
            }
        });
    }
}
