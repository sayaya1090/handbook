package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.AttributeValue;
import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.handbook.usecase.SearchProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * 에이전트가 현재 캔버스의 타입을 검색할 수 있도록 한다.
 * 쿼리가 비어있으면 전체 타입 목록, 아니면 이름/속성명으로 필터링한다.
 */
@Singleton
public class TypeSearchProvider implements SearchProvider {
    private final TypeList typeList;

    @Inject TypeSearchProvider(TypeList typeList) {
        this.typeList = typeList;
    }

    @Override
    public Observable<String> search(String query) {
        Set<TypeValue> types = typeList.getValue();
        String q = (query == null) ? "" : query.trim().toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"results\":[");
        boolean first = true;
        for (TypeValue t : types) {
            if (!q.isEmpty() && !matches(t, q)) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"key\":\"").append(escape(t.key())).append("\"");
            sb.append(",\"id\":\"").append(escape(t.id)).append("\"");
            sb.append(",\"version\":\"").append(escape(t.version)).append("\"");
            if (t.description != null) sb.append(",\"description\":\"").append(escape(t.description)).append("\"");
            sb.append(",\"attributeCount\":").append(t.attributes != null ? t.attributes.length : 0);
            sb.append("}");
        }
        sb.append("]}");
        return BehaviorSubject.<String>behavior(sb.toString()).asObservable();
    }

    private boolean matches(TypeValue t, String q) {
        if (t.id.toLowerCase().contains(q)) return true;
        if (t.description != null && t.description.toLowerCase().contains(q)) return true;
        if (t.attributes != null) {
            for (AttributeValue a : t.attributes) {
                if (a.name.toLowerCase().contains(q)) return true;
            }
        }
        return false;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
