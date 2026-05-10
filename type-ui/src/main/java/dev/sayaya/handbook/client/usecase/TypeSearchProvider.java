package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.usecase.SearchProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 캔버스의 현재 가시성(Visibility)과 검색을 관리하는 공급자.
 *
 * <p><b>책임:</b>
 * 1. {@link TypeList}와 {@link LayoutProvider}를 결합하여 현재 레이아웃 기간에 유효한 타입 목록을 필터링하여 제공한다.
 * 2. 에이전트가 현재 캔버스의 타입을 검색할 수 있도록 JSON 결과를 반환한다.
 * </p>
 */
@Singleton
public class TypeSearchProvider implements SearchProvider {
    private final TypeList typeList;
    private final LayoutProvider layoutProvider;
    private final BehaviorSubject<Set<Type>> visibleTypes = behavior(Collections.emptySet());

    @Inject TypeSearchProvider(TypeList typeList, LayoutProvider layoutProvider) {
        this.typeList = typeList;
        this.layoutProvider = layoutProvider;
        
        // 데이터 소스 변경 시 가시성 필터링 수행
        typeList.subscribe(types -> filterVisible());
        layoutProvider.subscribe(layout -> filterVisible());
    }

    public Observable<Set<Type>> visibleTypes() {
        return visibleTypes.asObservable();
    }

    public Set<Type> getVisibleTypes() {
        return visibleTypes.getValue();
    }

    private void filterVisible() {
        Set<Type> all = typeList.getValue();
        TypeLayout layout = layoutProvider.getValue();
        if (all == null || layout == null) {
            visibleTypes.next(Collections.emptySet());
            return;
        }

        double start = layout.effectDateTime();
        Set<Type> filtered = new HashSet<>();
        for (Type t : all) {
            // UC-T27 필터링 규칙: Type.effect <= Layout.start < Type.expire
            if (t.effectDateTime() <= start + 0.1 && t.expireDateTime() > start + 0.1) {
                filtered.add(t);
            }
        }
        visibleTypes.next(filtered);
    }

    @Override
    public Observable<String> search(String query) {
        Set<Type> types = visibleTypes.getValue();
        String q = (query == null) ? "" : query.trim().toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"results\":[");
        boolean first = true;
        for (Type t : types) {
            if (!q.isEmpty() && !matches(t, q)) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"key\":\"").append(escape(t.key())).append("\"");
            sb.append(",\"id\":\"").append(escape(t.id())).append("\"");
            sb.append(",\"version\":\"").append(escape(t.version())).append("\"");
            if (t.description() != null) sb.append(",\"description\":\"").append(escape(t.description())).append("\"");
            sb.append(",\"attributeCount\":").append(t.attributes() != null ? t.attributes().length : 0);
            sb.append("}");
        }
        sb.append("]}");
        return behavior(sb.toString()).asObservable();
    }

    private boolean matches(Type t, String q) {
        if (t.id().toLowerCase().contains(q)) return true;
        if (t.description() != null && t.description().toLowerCase().contains(q)) return true;
        return false;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
