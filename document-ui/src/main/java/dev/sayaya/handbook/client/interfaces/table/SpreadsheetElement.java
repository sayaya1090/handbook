package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.ColumnDef;
import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.usecase.ViewportObserver;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.div;

/**
 * Handsontable을 래핑하는 스프레드시트 요소.
 *
 * <p><b>책임:</b> {@link Handsontable} 인스턴스를 생성/관리하고, {@link DocumentList}의
 * 문서 데이터를 2D 배열로 변환하여 테이블에 렌더링한다. 컬럼 정의 변경 및
 * 뷰포트 크기에 따른 고정 컬럼 설정도 처리한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentList} — 표시할 문서 목록 상태 구독</li>
 *   <li>{@link dev.sayaya.handbook.usecase.ViewportObserver} — 모바일 여부에 따른 고정 컬럼 전환</li>
 *   <li>{@link Handsontable} — 실제 스프레드시트 JS 라이브러리 인스턴스</li>
 *   <li>{@link HandsontableConfig} — 테이블 설정 객체</li>
 * </ul></p>
 *
 * <p><b>주의:</b> init() 호출 전에는 테이블이 렌더링되지 않는다. init()은 컬럼 정의가
 * 결정된 후 한 번 호출해야 하며, 이후 컬럼 변경은 updateColumns()를 사용한다.</p>
 */
@Singleton
public class SpreadsheetElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement container;
    private final DocumentList documentList;
    private final ViewportObserver viewport;
    private Handsontable instance;
    private ColumnDef[] columns;

    @Inject
    public SpreadsheetElement(DocumentList documentList, ViewportObserver viewport) {
        this.container = div().css("doc-spreadsheet").element();
        this.documentList = documentList;
        this.viewport = viewport;
    }

    public void init(ColumnDef[] columns) {
        this.columns = columns;
        if (instance != null) instance.destroy();

        HandsontableConfig config = new HandsontableConfig();
        config.data = toData(documentList.getValue());
        config.columns = toColumns(columns);
        config.colHeaders = toHeaders(columns);
        config.stretchH = "all";
        config.manualColumnResize = true;
        config.autoRowSize = true;
        config.licenseKey = "non-commercial-and-evaluation";

        // 모바일: serial 컬럼 고정
        if (viewport.isMobileNow()) {
            config.fixedColumnsLeft = 1;
        }

        instance = new Handsontable(container, config);

        documentList.asObservable().subscribe(docs -> {
            if (instance != null) {
                config.data = toData(docs);
                instance.updateSettings(config);
            }
        });

        // 뷰포트 변경 시 고정 컬럼 업데이트
        viewport.isMobile().subscribe(mobile -> {
            if (instance != null) {
                HandsontableConfig s = instance.getSettings();
                s.fixedColumnsLeft = mobile ? 1 : null;
                instance.updateSettings(s);
            }
        });
    }

    public void updateColumns(ColumnDef[] columns) {
        this.columns = columns;
        if (instance != null) {
            HandsontableConfig config = instance.getSettings();
            config.columns = toColumns(columns);
            config.colHeaders = toHeaders(columns);
            instance.updateSettings(config);
        }
    }

    @Override
    public elemental2.dom.HTMLElement element() { return container; }

    private Object[][] toData(List<DocumentValue> docs) {
        if (docs == null || docs.isEmpty() || columns == null) return new Object[0][0];
        Object[][] result = new Object[docs.size()][columns.length];
        for (int r = 0; r < docs.size(); r++) {
            DocumentValue doc = docs.get(r);
            for (int c = 0; c < columns.length; c++) {
                String name = columns[c].name();
                result[r][c] = switch (name) {
                    case "serial" -> doc.serial;
                    case "effectDateTime" -> doc.effectDateTime;
                    case "expireDateTime" -> doc.expireDateTime;
                    default -> doc.data != null ? doc.data.get(name) : null;
                };
            }
        }
        return result;
    }

    private Column[] toColumns(ColumnDef[] defs) {
        Column[] cols = new Column[defs.length];
        for (int i = 0; i < defs.length; i++) {
            cols[i] = new Column();
            cols[i].data = String.valueOf(i);
            cols[i].type = defs[i].type();
            cols[i].width = defs[i].width();
            cols[i].readOnly = defs[i].readOnly();
        }
        return cols;
    }

    private String[] toHeaders(ColumnDef[] defs) {
        String[] headers = new String[defs.length];
        for (int i = 0; i < defs.length; i++) {
            headers[i] = defs[i].label();
        }
        return headers;
    }
}
