package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.Observable;

/**
 * 워크스페이스 CRUD 및 참여 요청 포트 인터페이스.
 *
 * <p><b>책임:</b> 워크스페이스 생성, 수정, 삭제, 참여(join) 요청을 정의한다.</p>
 * <p><b>의존관계:</b> {@link dev.sayaya.handbook.client.interfaces.api.WorkspaceApi}가 구현한다.</p>
 */
public interface WorkspaceRepository {
    Observable<String> create(String name, String description);
    Observable<String> update(String id, String name, String description);
    Observable<Void> delete(String id);

    /**
     * 기존 워크스페이스에 참여를 요청한다.
     *
     * @param workspaceId 참여할 워크스페이스 ID
     * @return 참여 요청 결과 (성공 시 완료, 실패 시 에러)
     */
    Observable<Void> join(String workspaceId);
}
