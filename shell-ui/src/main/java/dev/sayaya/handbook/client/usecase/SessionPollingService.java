package dev.sayaya.handbook.client.usecase;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * JWT 토큰 만료를 감시하고 자동 갱신을 수행하는 세션 관리자.
 *
 * <p><b>책임:</b> 주기적으로 JWT 만료 시각을 확인하여 만료 전 80% 시점에 토큰을 갱신하고,
 * 만료 5분 전에 경고 토스트를 표시하며, 만료 시 로그인 페이지로 리다이렉트한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link FetchApi} — 토큰 갱신 API 호출</li>
 *   <li>{@link ToastContainer} — 만료 경고 토스트 표시</li>
 *   <li>{@link LabelProvider} — 다국어 메시지</li>
 * </ul></p>
 *
 * <p><b>주의:</b> JWT duration은 기본 3600초(1시간). 체크 간격은 60초.
 * 쿠키에서 JWT exp 클레임을 읽어 만료 시각을 판단한다.</p>
 */
@Singleton
public class SessionPollingService {
    private static final int CHECK_INTERVAL_MS = 60_000;
    private static final int WARNING_BEFORE_EXPIRY_MS = 5 * 60 * 1000;
    private static final double REFRESH_THRESHOLD = 0.8;
    private static final String LOGIN_PATH = "/auth/login";

    private final FetchApi fetchApi;
    private final ToastContainer toastContainer;
    private final LabelProvider labelProvider;
    private Labels labels = Labels.empty();
    private boolean warningShown = false;
    private double timerHandle = -1;

    @Inject
    SessionPollingService(FetchApi fetchApi, ToastContainer toastContainer, LabelProvider labelProvider) {
        this.fetchApi = fetchApi;
        this.toastContainer = toastContainer;
        this.labelProvider = labelProvider;
        this.labelProvider.subscribe(l -> this.labels = l);
    }

    /**
     * 세션 감시를 시작한다. ShellInitializer 에서 호출된다.
     *
     * <p><b>2026-04 임시 비활성:</b> JWT 쿠키 부재 시 {@code /auth/login} 으로 무한 바운스
     * 되는 UX 이슈로 자동 체크 타이머를 설치하지 않는다. 토큰 만료 감지·갱신·리다이렉트
     * 로직은 향후 서버 주도(401 응답 기반) 또는 명시적 Sign In 메뉴 클릭 경로로 재설계
     * 예정. 호출 지점은 그대로 유지해 롤백 용이성 확보.</p>
     */
    public void initialize() {
        GWT.log("SessionPollingService: auto session polling disabled (2026-04 temporary).");
    }

    private void checkSession() {
        double expiry = getTokenExpiry();
        if (expiry <= 0) {
            // 토큰이 없거나 파싱 불가 — 로그인 페이지로 이동
            redirectToLogin();
            return;
        }
        double now = System.currentTimeMillis();
        double remaining = expiry - now;

        if (remaining <= 0) {
            redirectToLogin();
            return;
        }

        if (remaining <= WARNING_BEFORE_EXPIRY_MS && !warningShown) {
            warningShown = true;
            int minutesLeft = (int) Math.ceil(remaining / 60_000.0);
            toastContainer.show(
                ToastLevel.WARNING,
                labels.get("session_expiry_warning") + " (" + minutesLeft + " min)",
                (int) remaining
            );
        }

        // 토큰 수명의 80%가 경과하면 갱신
        double duration = getTokenDuration();
        if (duration > 0 && remaining <= duration * (1 - REFRESH_THRESHOLD)) {
            refreshToken();
        }
    }

    private void refreshToken() {
        var request = RequestInit.create();
        request.setMethod("POST");
        fetchApi.request("auth/refresh", request)
            .then(response -> {
                if (response.ok) {
                    warningShown = false;
                    GWT.log("SessionPollingService: token refreshed");
                } else if (response.status == 401) {
                    redirectToLogin();
                }
                return null;
            })
            .catch_(error -> {
                GWT.log("SessionPollingService: refresh failed - " + error);
                return null;
            });
    }

    private void redirectToLogin() {
        if (timerHandle >= 0) {
            DomGlobal.clearInterval(timerHandle);
            timerHandle = -1;
        }
        DomGlobal.window.location.assign(LOGIN_PATH);
    }

    /**
     * JWT 쿠키에서 만료 시각(ms)을 추출한다.
     * JWT payload의 exp 클레임을 Base64 디코딩하여 읽는다.
     *
     * @return 만료 시각(ms), 실패 시 -1
     */
    private static double getTokenExpiry() {
        jsinterop.base.JsPropertyMap<?> payload = parseJwtPayload();
        if (payload == null) return -1;
        jsinterop.base.Any exp = (jsinterop.base.Any) payload.get("exp");
        if (exp == null) return -1;
        return exp.asDouble() * 1000;
    }

    /**
     * JWT 쿠키에서 토큰 전체 수명(ms)을 계산한다 (exp - iat).
     *
     * @return 수명(ms), 실패 시 3600000 (기본 1시간)
     */
    private static double getTokenDuration() {
        jsinterop.base.JsPropertyMap<?> payload = parseJwtPayload();
        if (payload == null) return 3600000;
        jsinterop.base.Any exp = (jsinterop.base.Any) payload.get("exp");
        jsinterop.base.Any iat = (jsinterop.base.Any) payload.get("iat");
        if (exp == null || iat == null) return 3600000;
        return (exp.asDouble() - iat.asDouble()) * 1000;
    }

    /**
     * 쿠키에서 JWT 토큰을 찾아 payload를 파싱한다.
     * "token=" 또는 "Authorization=" 쿠키에서 JWT를 추출하고,
     * Base64 디코딩 후 JSON.parse()로 payload를 반환한다.
     *
     * @return JWT payload를 담은 JsPropertyMap, 실패 시 null
     */
    private static jsinterop.base.JsPropertyMap<?> parseJwtPayload() {
        try {
            String cookies = ((elemental2.dom.HTMLDocument) DomGlobal.document).cookie;
            if (cookies == null || cookies.isEmpty()) return null;
            String[] parts = cookies.split(";");
            for (String part : parts) {
                String cookie = part.trim();
                if (cookie.startsWith("token=") || cookie.startsWith("Authorization=")) {
                    String token = cookie.substring(cookie.indexOf('=') + 1);
                    String[] jwtParts = token.split("\\.");
                    if (jwtParts.length >= 2) {
                        String decoded = atob(jwtParts[1]);
                        Object parsed = elemental2.core.Global.JSON.parse(decoded);
                        return jsinterop.base.Js.cast(parsed);
                    }
                }
            }
        } catch (Exception e) {
            // 파싱 실패
        }
        return null;
    }

    /** window.atob()를 호출하여 Base64 문자열을 디코딩한다. */
    private static String atob(String encoded) {
        return jsinterop.base.Js.<AtobWindow>cast(DomGlobal.window).atob(encoded);
    }

    /** window.atob() 접근용 JsInterop 타입. */
    @jsinterop.annotations.JsType(isNative = true, namespace = jsinterop.annotations.JsPackage.GLOBAL, name = "Window")
    private static class AtobWindow {
        public native String atob(String encoded);
    }
}
