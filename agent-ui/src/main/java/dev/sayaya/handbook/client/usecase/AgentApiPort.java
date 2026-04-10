package dev.sayaya.handbook.client.usecase;

/**
 * 에이전트 백엔드 통신 포트.
 * interfaces 계층에서 구현하여 Gateway를 통해 Assistant 서비스와 통신한다.
 */
public interface AgentApiPort {
    /** 자연어 요청을 전송하고 SSE 스트림을 시작한다 */
    void startSession(String workspace, String request);

    /** await_confirm에 대한 사용자 응답을 전송한다 */
    void respond(String workspace, String response);

    /** 실행 중인 세션을 중단한다 */
    void abort(String workspace);
}
