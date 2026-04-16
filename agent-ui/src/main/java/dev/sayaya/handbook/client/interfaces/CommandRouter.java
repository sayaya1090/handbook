package dev.sayaya.handbook.client.interfaces;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.handbook.usecase.AttentionStyle;
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
 * <p><b>책임:</b> JSON.parse()로 커맨드 타입을 판별하고, navigate/highlight/attention/scroll/preview/mutate/notify/progress/await_confirm/complete를 각각의 Subject에 발행한다.</p>
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
    private final dev.sayaya.rx.subject.BehaviorSubject<ConfirmRequest> confirmSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<ProgressInfo> progressSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String[]> previewSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<dev.sayaya.handbook.client.domain.CompleteInfo> completeSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String> highlightSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String> scrollSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<NavigateInfo> navigateSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<String[]> mutateSubject = behavior(null);
    private final dev.sayaya.rx.subject.BehaviorSubject<NotifyInfo> notifySubject = behavior(null);
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
                onNavigate(getString(cmd, "menu"), getString(cmd, "tool"), getString(cmd, "url"));
                break;
            case "highlight":
                onHighlight(getString(cmd, "target"));
                break;
            case "attention":
                onAttention(getString(cmd, "target"),
                        getStringOrDefault(cmd, "style", "PULSE"),
                        getStringOrDefault(cmd, "message", ""),
                        getStringOrDefault(cmd, "position", "bottom"),
                        isTruthy(cmd.get("dismissable")));
                break;
            case "scroll":
                onScroll(getString(cmd, "target"));
                break;
            case "preview":
                previewSubject.next(toStringArrayFromAny(cmd.get("changes")));
                break;
            case "mutate":
                mutateSubject.next(toStringArrayFromAny(cmd.get("changes")));
                break;
            case "notify":
                onNotify(getStringOrDefault(cmd, "level", "info"),
                        getStringOrDefault(cmd, "message", ""));
                break;
            case "progress":
                onProgress(getDouble(cmd, "currentGroup"), getDouble(cmd, "totalGroups"),
                        (int) getDouble(cmd, "parallel"), (int) getDouble(cmd, "stepCount"));
                break;
            case "await_confirm":
                onAwaitConfirm(getStringOrDefault(cmd, "description", ""),
                        toStringArrayFromAny(cmd.get("options")));
                break;
            case "complete": {
                Object artifact = cmd.get("artifact");
                String artifactJson = artifact != null ? elemental2.core.Global.JSON.stringify(artifact) : null;
                onComplete(getStringOrDefault(cmd, "summary", ""),
                        getStringOrDefault(cmd, "executionId", ""), artifactJson);
                break;
            }
            case "search":
                onSearch(getString(cmd, "navigateTo"),
                        getStringOrDefault(cmd, "query", ""),
                        toStringArrayFromAny(cmd.get("targets")),
                        getStringOrDefault(cmd, "summary", ""));
                break;
            default:
                break;
        }
    }

    private void onNavigate(String menu, String tool, String url) {
        navigateSubject.next(new NavigateInfo(menu, tool, url));
    }
    private void onHighlight(String target) {
        highlightSubject.next(target);
    }
    private void onAttention(String target, String style, String message, String position, boolean dismissable) {
        AttentionStyle attentionStyle;
        try {
            attentionStyle = AttentionStyle.valueOf(style);
        } catch (IllegalArgumentException e) {
            attentionStyle = AttentionStyle.PULSE;
        }
        overlaySubject.next(new OverlayRequest(target, attentionStyle, message, position, dismissable));
    }
    private void onScroll(String target) {
        scrollSubject.next(target);
    }
    private void onNotify(String level, String message) {
        notifySubject.next(new NotifyInfo(level, message));
    }
    private void onSearch(String navigateTo, String query, String[] targets, String summary) {
        searchSubject.next(new SearchVisualizationRequest(navigateTo, query, targets, summary));
    }
    private void onProgress(double currentGroup, double totalGroups, int parallel, int stepCount) {
        progressSubject.next(new ProgressInfo(null, currentGroup, totalGroups, parallel, stepCount));
    }
    private void onAwaitConfirm(String description, String[] options) {
        session.stateObserver().next(AgentSessionState.AWAITING_CONFIRM);
        confirmSubject.next(new ConfirmRequest(description, options));
    }
    private void onComplete(String summary, String executionId, String artifactJson) {
        session.stateObserver().next(AgentSessionState.COMPLETED);
        CompleteInfo info = parseCompleteInfo(summary, executionId, artifactJson);
        completeSubject.next(info);
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

    /** JS truthy 검사를 수행한다. */
    private static boolean isTruthy(Object val) {
        return val != null && Js.isTruthy(val);
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
    @Override public Observable<ConfirmRequest> confirmRequests() { return confirmSubject; }
    @Override public Observable<ProgressInfo> progressUpdates() { return progressSubject; }
    @Override public Observable<String[]> previewRequests() { return previewSubject; }
    @Override public Observable<CompleteInfo> completions() { return completeSubject; }
    @Override public Observable<String> highlights() { return highlightSubject; }
    @Override public Observable<String> scrollTargets() { return scrollSubject; }
    @Override public Observable<NavigateInfo> navigations() { return navigateSubject; }
    @Override public Observable<String[]> mutations() { return mutateSubject; }
    @Override public Observable<NotifyInfo> notifications() { return notifySubject; }
    @Override public Observable<SearchVisualizationRequest> searchVisualizations() { return searchSubject; }
}
