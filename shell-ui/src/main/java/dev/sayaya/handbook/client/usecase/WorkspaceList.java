package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 현재 사용자의 워크스페이스 목록 상태.
 *
 * <p>{@link WorkspaceRepository#list()} 를 구독해 원본을 그대로 전파한다.
 * {@link User}(identity) 와 분리된 별도 엔드포인트에서 받는다 — /user 는 id/name 만,
 * /workspaces 가 목록 전담. search-workspace 가 principal.sub 기반 필터링을 수행하므로
 * 이 컴포넌트는 결과를 검증 없이 publish.</p>
 */
@Singleton
public class WorkspaceList {
    @Delegate private final BehaviorSubject<List<Workspace>> _this = behavior(List.of());
    @Inject WorkspaceList(WorkspaceRepository repo) {
        repo.list().subscribe(list -> next(list == null ? List.of() : list));
    }
}
