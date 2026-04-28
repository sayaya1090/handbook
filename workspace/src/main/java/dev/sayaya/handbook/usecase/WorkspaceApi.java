package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.Group;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.rx.Observable;

/**
 * 워크스페이스 관리 API 인터페이스.
 * 백엔드와 프론트엔드 공용 계약.
 */
public interface WorkspaceApi {
    Observable<Workspace[]> list();
    Observable<Workspace> create(String name, String description);
    Observable<Workspace> update(String id, String name, String description);
    
    Observable<Group[]> listGroups(String workspaceId);
    Observable<Group> createGroup(String workspaceId, String name, String description);
    Observable<Void> deleteGroup(String workspaceId, String groupId);
    
    Observable<User[]> listMembers(String workspaceId, String groupId);
    Observable<Void> addMember(String workspaceId, String groupId, String userId);
    Observable<Void> removeMember(String workspaceId, String groupId, String userId);
    
    Observable<User[]> searchUsers(String query);
}
