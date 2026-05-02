package dev.sayaya.handbook.client;

import dev.sayaya.handbook.usecase.FetchApi;
import elemental2.core.Global;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.dom.ResponseInit;
import elemental2.promise.Promise;
import jsinterop.base.JsPropertyMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FetchApi 구현체로서 URL별 모의 응답을 동적으로 반환한다.
 * 각 요청마다 새로운 Response 객체를 생성하여 'body stream already read' 에러를 방지한다.
 */
public class FetchMock implements FetchApi {
    private static class MockData {
        final int status;
        String jsonBody;

        MockData(int status, Object payload) {
            this.status = status;
            this.jsonBody = Global.JSON.stringify(payload);
        }
    }

    private final Map<String, MockData> mocks = new LinkedHashMap<>();

    public FetchMock() {
        // 기본값으로 2개의 목업 문서 세팅
        setDocuments(createMockDocuments());
    }

    private static Object createMockDocuments() {
        JsPropertyMap<Object> doc1 = JsPropertyMap.of();
        doc1.set("id", "doc-1");
        doc1.set("serial", "CUST-001");
        doc1.set("status", "DRAFT");
        JsPropertyMap<Object> data1 = JsPropertyMap.of();
        data1.set("name", "User A");
        doc1.set("data", data1);

        JsPropertyMap<Object> doc2 = JsPropertyMap.of();
        doc2.set("id", "doc-2");
        doc2.set("serial", "CUST-002");
        doc2.set("status", "PUBLISHED");
        JsPropertyMap<Object> data2 = JsPropertyMap.of();
        data2.set("name", "User B");
        doc2.set("data", data2);

        return new Object[] { doc1, doc2 };
    }

    /** 테스트 코드에서 동적으로 응답 데이터를 교체할 수 있도록 노출 */
    public void setDocuments(Object payload) {
        mocks.put("documents", new MockData(200, payload));
    }

    public FetchMock when(String pathContains, int status, Object jsonPayload) {
        mocks.put(pathContains, new MockData(status, jsonPayload));
        return this;
    }

    @Override
    public Promise<Response> request(String url, RequestInit param) {
        if (url != null) {
            for (Map.Entry<String, MockData> entry : mocks.entrySet()) {
                if (url.contains(entry.getKey())) {
                    return Promise.resolve(createResponse(entry.getValue()));
                }
            }
        }
        return Promise.resolve(createEmptyResponse(404));
    }

    private static Response createResponse(MockData data) {
        ResponseInit init = ResponseInit.create();
        init.setStatus(data.status);
        init.setStatusText(data.status == 200 ? "OK" : "Error");
        return new Response(data.jsonBody, init);
    }

    private static Response createEmptyResponse(int status) {
        ResponseInit init = ResponseInit.create();
        init.setStatus(status);
        init.setStatusText("Not Found");
        return new Response((String) null, init);
    }
}
