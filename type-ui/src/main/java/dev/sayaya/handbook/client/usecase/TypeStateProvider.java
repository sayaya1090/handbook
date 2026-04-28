package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.AttributeValue;
import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.handbook.usecase.StateProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * 캔버스의 현재 타입 상태를 JSON 문자열로 제공한다.
 * 에이전트가 현재 편집 중인 타입/속성 정보를 파악할 수 있도록 한다.
 */
@Singleton
public class TypeStateProvider implements StateProvider {
    private final TypeList typeList;

    @Inject
    TypeStateProvider(TypeList typeList) {
        this.typeList = typeList;
    }

    @Override
    public String snapshot() {
        Set<TypeValue> types = typeList.getValue();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"types\":[");
        boolean first = true;
        for (TypeValue t : types) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"id\":\"").append(escape(t.id)).append("\"");
            sb.append(",\"version\":\"").append(escape(t.version)).append("\"");
            sb.append(",\"key\":\"").append(escape(t.key())).append("\"");
            if (t.description != null) {
                sb.append(",\"description\":\"").append(escape(t.description)).append("\"");
            }
            sb.append(",\"attributes\":[");
            if (t.attributes != null) {
                boolean afirst = true;
                for (AttributeValue a : t.attributes) {
                    if (!afirst) sb.append(",");
                    afirst = false;
                    sb.append("{\"name\":\"").append(escape(a.name)).append("\"");
                    sb.append(",\"order\":").append(a.order);
                    if (a.type != null) {
                        sb.append(",\"type\":\"").append(escape(a.type.simplify())).append("\"");
                    }
                    sb.append(",\"nullable\":").append(a.nullable);
                    sb.append("}");
                }
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
