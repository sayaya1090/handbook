package dev.sayaya.handbook.client.components;

import java.util.Arrays;
import java.util.Set;

/**
 * RBAC 권한 검증 유틸리티.
 *
 * <p><b>책임:</b> 사용자의 역할(roles) 집합과 요구되는 쓰기 권한(requiredWriteRoles)을 비교하여
 * 읽기 전용 여부를 판단한다. 역할이 없거나 권한 정보를 가져올 수 없는 경우
 * 안전하게 읽기 전용 모드로 전환한다.</p>
 *
 * <p><b>주의:</b> GWT 환경에서 사용되므로 Java 표준 라이브러리만 사용한다.
 * 역할 문자열은 JWT claims에서 파싱되어 전달된다.
 * 패턴: {@code {workspace}:type:{type}:document:edit} 또는 {@code {workspace}:type:{type}:edit}</p>
 */
public final class RbacGuard {
    private RbacGuard() {}

    /**
     * 사용자가 쓰기 권한을 가지지 않는지 판단한다.
     *
     * @param userRoles 사용자가 보유한 역할 집합 (JWT claims에서 추출)
     * @param requiredWriteRoles 쓰기에 필요한 역할 중 하나 이상 (OR 조건)
     * @return 사용자가 쓰기 권한이 없으면 true (읽기 전용), 하나라도 보유하면 false
     */
    public static boolean isReadOnly(Set<String> userRoles, String... requiredWriteRoles) {
        if (userRoles == null || userRoles.isEmpty()) return true;
        if (requiredWriteRoles == null || requiredWriteRoles.length == 0) return false;
        return Arrays.stream(requiredWriteRoles).noneMatch(userRoles::contains);
    }

    /**
     * 워크스페이스 + 타입 조합으로 문서 편집 권한을 확인한다.
     *
     * @param userRoles 사용자 역할 집합
     * @param workspace 워크스페이스 ID
     * @param type 타입 이름
     * @return 문서 편집 권한이 없으면 true
     */
    public static boolean isDocumentReadOnly(Set<String> userRoles, String workspace, String type) {
        String requiredRole = workspace + ":type:" + type + ":document:edit";
        return isReadOnly(userRoles, requiredRole);
    }

    /**
     * 워크스페이스 + 타입 조합으로 타입 편집 권한을 확인한다.
     *
     * @param userRoles 사용자 역할 집합
     * @param workspace 워크스페이스 ID
     * @param type 타입 이름
     * @return 타입 편집 권한이 없으면 true
     */
    public static boolean isTypeReadOnly(Set<String> userRoles, String workspace, String type) {
        String requiredRole = workspace + ":type:" + type + ":edit";
        return isReadOnly(userRoles, requiredRole);
    }
}
