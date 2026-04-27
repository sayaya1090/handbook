package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.rx.Observable;

import java.util.List;

/**
 * 현재 사용자가 소속된 워크스페이스 목록 read port.
 *
 * <p><b>경계:</b> /user 응답(identity) 과 분리된 독립 엔드포인트 소비. search-workspace 의
 * {@code GET /workspaces} 가 principal.sub 기반으로 필터링된 결과를 반환하므로, shell-ui 는
 * 이 포트를 통해 목록만 구독한다. {@link UserRepository} 는 id/name/profile 만 담당.</p>
 */
public interface WorkspaceRepository {
    Observable<List<Workspace>> list();
}
