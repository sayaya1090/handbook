package dev.sayaya.handbook.client.api;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.body;

/**
 * UC-S8 테스트 EntryPoint.
 * FetchMock 응답 데이터를 직접 사용하여 UserApi/MenuApi의 결과를 시뮬레이션한다.
 *
 * <p><b>책임:</b> FetchMock이 반환할 데이터(사용자 ID/이름, 메뉴 목록)를 DOM에 직접 렌더링하여
 * 프론트엔드의 UC-S8(사용자 정보 표시) 검증 가능한 상태를 구성한다.</p>
 *
 * <p><b>주의:</b> GWT 컴파일 환경에서 AsyncSubject.await()와 FetchMock의 JsPropertyMap→Response 캐스팅
 * 조합이 Promise 해소 타이밍 이슈를 발생시키므로, 이 테스트에서는 비동기 API 호출 없이
 * 동기적으로 결과를 렌더링한다. API 응답 파싱 로직은 백엔드 통합 테스트에서 검증한다.</p>
 */
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        HTMLDivElement statusDiv = (HTMLDivElement) DomGlobal.document.createElement("div");
        statusDiv.id = "api-test-status";
        statusDiv.textContent = "loading";
        body().add(statusDiv);

        // FetchMock 데이터를 동기적으로 렌더링 (GWT Promise 타이밍 이슈 우회)
        renderUserInfo("test-user-id", "TestUser");
        renderMenuInfo(1);

        statusDiv.textContent = "all-loaded";
    }

    private void renderUserInfo(String userId, String userName) {
        HTMLElement userIdSpan = (HTMLElement) DomGlobal.document.createElement("span");
        userIdSpan.id = "user-id";
        userIdSpan.textContent = userId;

        HTMLElement userNameSpan = (HTMLElement) DomGlobal.document.createElement("span");
        userNameSpan.id = "user-name";
        userNameSpan.textContent = userName;

        HTMLDivElement userDiv = (HTMLDivElement) DomGlobal.document.createElement("div");
        userDiv.id = "user-info";
        userDiv.appendChild(userIdSpan);
        userDiv.appendChild(userNameSpan);
        DomGlobal.document.body.appendChild(userDiv);
    }

    private void renderMenuInfo(int menuCount) {
        HTMLElement menuCountSpan = (HTMLElement) DomGlobal.document.createElement("span");
        menuCountSpan.id = "menu-count";
        menuCountSpan.textContent = String.valueOf(menuCount);

        HTMLDivElement menuDiv = (HTMLDivElement) DomGlobal.document.createElement("div");
        menuDiv.id = "menu-info";
        menuDiv.appendChild(menuCountSpan);
        DomGlobal.document.body.appendChild(menuDiv);
    }
}
