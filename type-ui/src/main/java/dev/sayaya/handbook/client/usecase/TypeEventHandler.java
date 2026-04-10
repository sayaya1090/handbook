package dev.sayaya.handbook.client.usecase;


import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ActionManager;
import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.usecase.WorkspaceEventReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 워크스페이스 SSE 이벤트를 구독하여 타입 목록을 자동 갱신한다.
 *
 * <p>TYPE_CREATED, TYPE_DELETED 이벤트 수신 시:
 * <ol>
 *   <li>현재 레이아웃 기간의 타입을 다시 조회한다.</li>
 *   <li>토스트 알림을 표시한다.</li>
 * </ol>
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

    @Inject
    public TypeEventHandler(WorkspaceEventReceiver eventReceiver,
                            TypeRepository typeRepository,
                            TypeList typeList,
                            LayoutProvider layoutProvider,
                            ChangeTracker tracker,
                            ActionManager actionManager,
                            ToastContainer toastContainer) {
        this.eventReceiver = eventReceiver;
        this.typeRepository = typeRepository;
        this.typeList = typeList;
        this.layoutProvider = layoutProvider;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.toastContainer = toastContainer;
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
                toastContainer.show(ToastLevel.INFO, "\ub2e4\ub978 \uc0ac\uc6a9\uc790\uac00 \ud0c0\uc785\uc744 \ubcc0\uacbd\ud588\uc2b5\ub2c8\ub2e4");
                break;
            default:
                break;
        }
    }

    private void refreshTypes() {
        LayoutPeriod period = layoutProvider.getValue();
        if (period == null) return;
        GWT.log("TypeEventHandler: refreshing types for current layout period");
        typeRepository.list(period).subscribe(types -> {
            if (types != null) {
                typeList.replace(types);
                tracker.reset();
                actionManager.clear();
            }
        });
    }
}
