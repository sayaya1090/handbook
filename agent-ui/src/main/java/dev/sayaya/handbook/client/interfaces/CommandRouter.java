package dev.sayaya.handbook.client.interfaces;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.handbook.domain.*;
import dev.sayaya.rx.Observable;
import elemental2.core.JsArray;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * SSE로 수신된 JSON 커맨드를 파싱하여 타입별 BehaviorSubject에 발행하는 라우터.
 *
 * <p><b>책임:</b> JSON.parse()로 커맨드 타입을 판별하고, agent-bridge 프로토콜 타입으로 캐스트한 뒤
 * navigate/highlight/attention/scroll/preview/mutate/notify/progress/await_confirm/complete를
 * 각각의 Subject에 발행한다. 단순 커맨드는 프로토콜 타입을 직접 발행하고,
 * 파생 로직이 필요한 커맨드(attention→OverlayRequest, progress→ProgressInfo, complete→CompleteInfo)는
 * agent-ui 도메인 타입으로 변환하여 발행한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentSession} — await_confirm/complete 시 세션 상태 전이</li>
 *   <li>{@link dev.sayaya.rx.subject.BehaviorSubject} — 커맨드별 반응형 스트림</li>
 * </ul></p>
 * <p><b>주의:</b> JSON 파싱은 Elemental2 Global.JSON을 사용하며 브라우저 환경에서만 동작한다.</p>
 */
@Singleton
public class CommandRouter implements AgentCommandDispatcher {
    private final AgentSession session;

    private final dev.sayaya.rx.subject.BehaviorSubject<OverlayRequest> overlaySubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<AwaitConfirmCommand> confirmSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<ProgressInfo> progressSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<PreviewCommand> previewSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<CompleteInfo> completeSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<HighlightCommand> highlightSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<ScrollCommand> scrollSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<NavigateCommand> navigateSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<MutateCommand> mutateSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<NotifyCommand> notifySubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<SearchVisualizationRequest> searchSubject = behavior(null);

    @Inject
    public CommandRouter(AgentSession session) {
        this.session = session;
    }

    public void route(String json) {
        try {
            routeCommand(json);
        } catch (Exception e) {
            GWT.log("Failed to route command: " + e.getMessage());
        }
    }

    /**
     * JSON 문자열을 파싱하여 커맨드 타입에 따라 적절한 핸들러를 호출한다.
     * 단순 커맨드는 Js.cast()로 프로토콜 타입에 직접 캐스트하고,
     * 파생 로직이 필요한 커맨드는 JsPropertyMap에서 필드를 추출하여 도메인 타입으로 변환한다.
     */
    private void routeCommand(String json) {
        Object parsed = elemental2.core.Global.JSON.parse(json);
        if (parsed == null) return;
        JsPropertyMap<?> cmd = Js.cast(parsed);
        Object typeObj = cmd.get("type");
        if (typeObj == null) return;
        String type = (String) typeObj;

        switch (type) {
            case "navigate":
                navigateSubject.next(Js.cast(parsed));
                break;
            case "highlight":
                highlightSubject.next(Js.cast(parsed));
                break;
            case "attention":
                onAttention(Js.cast(parsed));
                break;
            case "scroll":
                scrollSubject.next(Js.cast(parsed));
                break;
            case "preview":
                previewSubject.next(Js.cast(parsed));
                break;
            case "mutate":
                mutateSubject.next(Js.cast(parsed));
                break;
            case "notify":
                notifySubject.next(Js.cast(parsed));
                break;
            case "progress":
                onProgress(cmd);
                break;
            case "await_confirm":
                onAwaitConfirm(Js.cast(parsed));
                break;
            case "complete":
                onComplete(cmd);
                break;
            case "search":
                onSearch(cmd);
                break;
            default:
                break;
        }
    }

    private void onAttention(AttentionCommand cmd) {
        String styleStr = cmd.style() != null ? cmd.style() : "PULSE";
        AttentionStyle attentionStyle;
        try {
            attentionStyle = AttentionStyle.valueOf(styleStr);
        } catch (IllegalArgumentException e) {
            attentionStyle = AttentionStyle.PULSE;
        }
        String message = cmd.message() != null ? cmd.message() : "";
        String position = cmd.position() != null ? cmd.position() : "bottom";
        overlaySubject.next(new OverlayRequest(cmd.target(), attentionStyle, message, position, cmd.dismissable()));
    }

    private void onProgress(JsPropertyMap<?> cmd) {
        double currentGroup = getDouble(cmd, "currentGroup");
        double totalGroups = getDouble(cmd, "totalGroups");
        int parallel = (int) getDouble(cmd, "parallel");
        int stepCount = (int) getDouble(cmd, "stepCount");
        progressSubject.next(new ProgressInfo(null, currentGroup, totalGroups, parallel, stepCount));
    }

    private void onAwaitConfirm(AwaitConfirmCommand cmd) {
        session.stateObserver().next(AgentSessionState.AWAITING_CONFIRM);
        confirmSubject.next(cmd);
    }

    private void onComplete(JsPropertyMap<?> cmd) {
        session.stateObserver().next(AgentSessionState.COMPLETED);
        String summary = getStringOrDefault(cmd, "summary", "");
        String executionId = getStringOrDefault(cmd, "executionId", "");
        Object artifact = cmd.get("artifact");
        String artifactJson = artifact != null ? elemental2.core.Global.JSON.stringify(artifact) : null;
        CompleteInfo info = parseCompleteInfo(summary, executionId, artifactJson);
        completeSubject.next(info);
    }

    private void onSearch(JsPropertyMap<?> cmd) {
        String navigateTo = getString(cmd, "navigateTo");
        String query = getStringOrDefault(cmd, "query", "");
        String[] targets = toStringArrayFromAny(cmd.get("targets"));
        String summaryStr = getStringOrDefault(cmd, "summary", "");
        searchSubject.next(new SearchVisualizationRequest(navigateTo, query, targets, summaryStr));
    }

    private static CompleteInfo parseCompleteInfo(String summary, String executionId, String artifactJson) {
        if (artifactJson == null || artifactJson.isEmpty()) {
            return new CompleteInfo(summary, executionId, null);
        }
        CompleteInfo.ArtifactInfo artifact = parseArtifact(artifactJson);
        return new CompleteInfo(summary, executionId, artifact);
    }

    /**
     * JSON 문자열에서 ArtifactInfo를 파싱한다.
     * changes 배열의 각 요소에서 type, target, description 필드를 추출하여
     * ChangeEntry 배열을 구성한다.
     */
    private static CompleteInfo.ArtifactInfo parseArtifact(String json) {
        try {
            Object parsed = elemental2.core.Global.JSON.parse(json);
            JsPropertyMap<?> obj = Js.cast(parsed);
            String artifactSummary = getStringOrDefault(obj, "summary", "");
            Object changesObj = obj.get("changes");
            if (changesObj == null) {
                return new CompleteInfo.ArtifactInfo(artifactSummary, new CompleteInfo.ChangeEntry[0]);
            }
            JsArray<?> changes = Js.cast(changesObj);
            CompleteInfo.ChangeEntry[] entries = new CompleteInfo.ChangeEntry[changes.length];
            for (int i = 0; i < changes.length; i++) {
                JsPropertyMap<?> change = Js.cast(changes.getAt(i));
                entries[i] = new CompleteInfo.ChangeEntry(
                        getStringOrDefault(change, "type", ""),
                        getStringOrDefault(change, "target", ""),
                        getStringOrDefault(change, "description", "")
                );
            }
            return new CompleteInfo.ArtifactInfo(artifactSummary, entries);
        } catch (Exception e) {
            return null;
        }
    }

    // --- JsPropertyMap 유틸리티 ---

    /** JsPropertyMap에서 문자열 값을 꺼낸다. 없으면 null을 반환한다. */
    private static String getString(JsPropertyMap<?> map, String key) {
        Object val = map.get(key);
        return val != null ? (String) val : null;
    }

    /** JsPropertyMap에서 문자열 값을 꺼낸다. 없으면 기본값을 반환한다. */
    private static String getStringOrDefault(JsPropertyMap<?> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? (String) val : defaultVal;
    }

    /** JsPropertyMap에서 double 값을 꺼낸다. 없으면 0을 반환한다. */
    private static double getDouble(JsPropertyMap<?> map, String key) {
        Any val = (Any) map.get(key);
        return val != null ? val.asDouble() : 0;
    }

    /** JS 배열(또는 null)을 String[]로 변환한다. */
    private static String[] toStringArrayFromAny(Object jsVal) {
        if (jsVal == null) return new String[0];
        JsArray<String> arr = Js.cast(jsVal);
        String[] result = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr.getAt(i);
        }
        return result;
    }

    @Override public Observable<OverlayRequest> overlayRequests() { return overlaySubject; }
    @Override public Observable<AwaitConfirmCommand> confirmRequests() { return confirmSubject; }
    @Override public Observable<ProgressInfo> progressUpdates() { return progressSubject; }
    @Override public Observable<PreviewCommand> previewRequests() { return previewSubject; }
    @Override public Observable<CompleteInfo> completions() { return completeSubject; }
    @Override public Observable<HighlightCommand> highlights() { return highlightSubject; }
    @Override public Observable<ScrollCommand> scrollTargets() { return scrollSubject; }
    @Override public Observable<NavigateCommand> navigations() { return navigateSubject; }
    @Override public Observable<MutateCommand> mutations() { return mutateSubject; }
    @Override public Observable<NotifyCommand> notifications() { return notifySubject; }
    @Override public Observable<SearchVisualizationRequest> searchVisualizations() { return searchSubject; }
}
