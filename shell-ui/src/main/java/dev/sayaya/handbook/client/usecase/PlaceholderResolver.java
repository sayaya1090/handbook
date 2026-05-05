package dev.sayaya.handbook.client.usecase;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * 문자열 내의 예약어({@code {key}})를 {@link SessionContext} 의 실제 값으로 치환한다.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>메뉴의 {@code url} 필드 치환</li>
 *   <li>메뉴의 {@code urlRegex} 필드 치환 (정규식 매칭 전 필수)</li>
 *   <li>안전한 치환: 값이 null 인 경우 치환하지 않고 그대로 둔다.</li>
 * </ul></p>
 */
@Singleton
public class PlaceholderResolver {
    private final SessionContext context;

    @Inject public PlaceholderResolver(SessionContext context) {
        this.context = context;
    }

    /**
     * 문자열 내의 모든 {key} 패턴을 컨텍스트 값으로 치환한다.
     * @param template 원본 문자열 (예: "/workspaces/{workspaceId}/type")
     * @return 치환된 문자열 (예: "/workspaces/abc-123/type")
     */
    public String resolve(String template) {
        if (template == null) return null;
        String result = template;
        Map<String, String> values = context.getAll();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue();
            if (value != null) {
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }
}
