package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Group;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.rx.Observable;

/**
 * 워크스페이스 관리 API 포트.
 * 
 * **역할:** 워크스페이스 설정, 그룹 관리, 멤버 관리, 권한 관리 HTTP 호출 추상화.
 */
public interface WorkspaceApi {
    /** 워크스페이스 정보 수정 */
    Observable<Workspace> update(String id, String name, String description);
    
    /** 그룹 목록 조회 */
    Observable<Group[]> listGroups(String workspaceId);
    
    /** 그룹 생성 */
    Observable<Group> createGroup(String workspaceId, String name, String description);
    
    /** 그룹 삭제 */
    Observable<Void> deleteGroup(String workspaceId, String groupId);
    
    /** 그룹 멤버 목록 조회 */
    Observable<User[]> listMembers(String workspaceId, String groupId);
    
    /** 멤버 추가 */
    Observable<Void> addMember(String workspaceId, String groupId, String userId);
    
    /** 멤버 삭제 */
    Observable<Void> removeMember(String workspaceId, String groupId, String userId);
    
    /** 그룹의 역할 조회 */
    Observable<String[]> listRoles(String workspaceId, String groupId);
    
    /** 역할 부여 */
    Observable<Void> assignRole(String workspaceId, String groupId, String roleName);
    
    /** 역할 제거 */
    Observable<Void> removeRole(String workspaceId, String groupId, String roleName);
}
