package dev.sayaya.handbook.client.usecase;

/**
 * 에이전트 백엔드 통신 포트 인터페이스.
 *
 * <p><b>책임:</b> 세션 시작, 사용자 응답 전송, 세션 중단 등 에이전트 API 통신 계약을 정의한다.</p>
 * <p><b>의존관계:</b> <ul><li>interfaces 계층의 {@link dev.sayaya.handbook.client.interfaces.AgentSseClient}가 구현한다.</li></ul></p>
 */
public interface AgentApiPort {
    /** 자연어 요청을 전송하고 SSE 스트림을 시작한다 */
    void startSession(String workspace, String request);

    /** await_confirm에 대한 사용자 응답을 전송한다 */
    void respond(String workspace, String response);

    /** 실행 중인 세션을 중단한다 */
    void abort(String workspace);
}
