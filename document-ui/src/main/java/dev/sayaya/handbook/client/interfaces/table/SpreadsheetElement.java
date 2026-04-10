package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.ColumnDef;
import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.SelectedRows;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.usecase.LabelProvider;
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
 *   <li>{@link SelectedRows} — 체크박스로 선택된 행 인덱스 관리</li>
 *   <li>{@link dev.sayaya.handbook.usecase.ViewportObserver} — 모바일 여부에 따른 고정 컬럼 전환</li>
 *   <li>{@link Handsontable} — 실제 스프레드시트 JS 라이브러리 인스턴스</li>
 *   <li>{@link HandsontableConfig} — 테이블 설정 객체</li>
 *   <li>{@link LabelProvider} — 빈 상태 메시지 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> init() 호출 전에는 테이블이 렌더링되지 않는다. init()은 컬럼 정의가
 * 결정된 후 한 번 호출해야 하며, 이후 컬럼 변경은 updateColumns()를 사용한다.
 * 문서가 0건이면 테이블 대신 빈 상태 메시지를 표시한다.</p>
 */
@Singleton
public class SpreadsheetElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement container;
    private final elemental2.dom.HTMLDivElement emptyState;
    private final DocumentList documentList;
    private final SelectedRows selectedRows;
    private final ViewportObserver viewport;
    private Handsontable instance;
    private ColumnDef[] columns;
    private Labels currentLabels = Labels.empty();

    @Inject
    public SpreadsheetElement(DocumentList documentList, SelectedRows selectedRows,
                              ViewportObserver viewport, LabelProvider labelProvider) {
        this.container = div().css("doc-spreadsheet").element();
        this.emptyState = div().css("doc-empty-state").element();
        this.emptyState.style.set("display", "none");
        this.emptyState.style.set("text-align", "center");
        this.emptyState.style.set("padding", "48px 16px");
        this.emptyState.style.set("color", "var(--md-sys-color-on-surface-variant, #666)");
        this.container.appendChild(emptyState);
        this.documentList = documentList;
        this.selectedRows = selectedRows;
        this.viewport = viewport;
        labelProvider.subscribe(labels -> {
            currentLabels = labels;
            emptyState.textContent = labels.getOrDefault("document.empty", "No documents");
        });
    }

    public void init(ColumnDef[] columns) {
        this.columns = columns;
        if (instance != null) instance.destroy();

        HandsontableConfig config = new HandsontableConfig();
        config.data = toDataWithCheckbox(documentList.getValue());
        config.columns = toColumnsWithCheckbox(columns);
        config.colHeaders = toHeadersWithCheckbox(columns);
        config.stretchH = "all";
        config.manualColumnResize = true;
        config.autoRowSize = true;
        config.licenseKey = "non-commercial-and-evaluation";

        // 모바일: checkbox + serial 컬럼 고정
        if (viewport.isMobileNow()) {
            config.fixedColumnsLeft = 2;
        } else {
            config.fixedColumnsLeft = 1;
        }

        instance = new Handsontable(container, config);
        initCheckboxListener();

        documentList.asObservable().subscribe(docs -> {
            if (instance != null) {
                config.data = toDataWithCheckbox(docs);
                instance.updateSettings(config);
            }
            updateEmptyState(docs);
        });

        // 뷰포트 변경 시 고정 컬럼 업데이트 (checkbox + serial 또는 checkbox만)
        viewport.isMobile().subscribe(mobile -> {
            if (instance != null) {
                HandsontableConfig s = instance.getSettings();
                s.fixedColumnsLeft = mobile ? 2 : 1;
                instance.updateSettings(s);
            }
        });
    }

    public void updateColumns(ColumnDef[] columns) {
        this.columns = columns;
        if (instance != null) {
            HandsontableConfig config = instance.getSettings();
            config.columns = toColumnsWithCheckbox(columns);
            config.colHeaders = toHeadersWithCheckbox(columns);
            instance.updateSettings(config);
        }
    }

    private void updateEmptyState(List<DocumentValue> docs) {
        boolean empty = docs == null || docs.isEmpty();
        emptyState.style.set("display", empty ? "block" : "none");
    }

    @Override
    public elemental2.dom.HTMLElement element() { return container; }

    /**
     * Handsontable afterChange 이벤트에서 체크박스 변경을 감지하여 SelectedRows에 반영한다.
     * JsInterop을 통해 addHook API를 호출한다.
     */
    private void initCheckboxListener() {
        if (instance == null) return;
        instance.addHook("afterChange", (changes, source) -> {
            if (changes == null) return;
            for (int i = 0; i < changes.length; i++) {
                elemental2.core.JsArray<?> change = changes.getAt(i);
                int row = ((Double) change.getAt(0)).intValue();
                Object col = change.getAt(1);
                Object newVal = change.getAt(3);
                if ("0".equals(String.valueOf(col)) || Integer.valueOf(0).equals(col)) {
                    onCheckboxChange(row, jsinterop.base.Js.isTruthy(newVal));
                }
            }
        });
    }

    private void onCheckboxChange(int row, boolean checked) {
        selectedRows.toggle(row);
    }

    private Object[][] toDataWithCheckbox(List<DocumentValue> docs) {
        if (docs == null || docs.isEmpty() || columns == null) return new Object[0][0];
        int colCount = columns.length + 1; // +1 for checkbox
        Object[][] result = new Object[docs.size()][colCount];
        for (int r = 0; r < docs.size(); r++) {
            result[r][0] = Boolean.FALSE; // checkbox column
            DocumentValue doc = docs.get(r);
            for (int c = 0; c < columns.length; c++) {
                String name = columns[c].name();
                result[r][c + 1] = switch (name) {
                    case "serial" -> doc.serial;
                    case "effectDateTime" -> doc.effectDateTime;
                    case "expireDateTime" -> doc.expireDateTime;
                    default -> doc.data != null ? doc.data.get(name) : null;
                };
            }
        }
        return result;
    }

    private Column[] toColumnsWithCheckbox(ColumnDef[] defs) {
        Column[] cols = new Column[defs.length + 1];
        // 체크박스 컬럼
        cols[0] = new Column();
        cols[0].data = "0";
        cols[0].type = "checkbox";
        cols[0].width = 30;
        cols[0].readOnly = false;
        // 기존 컬럼 (인덱스 +1)
        for (int i = 0; i < defs.length; i++) {
            cols[i + 1] = new Column();
            cols[i + 1].data = String.valueOf(i + 1);
            cols[i + 1].type = defs[i].type();
            cols[i + 1].width = defs[i].width();
            cols[i + 1].readOnly = defs[i].readOnly();
            if (defs[i].source() != null) cols[i + 1].source = defs[i].source();
            if (defs[i].dateFormat() != null) cols[i + 1].dateFormat = defs[i].dateFormat();
            if (defs[i].correctFormat() != null) cols[i + 1].correctFormat = defs[i].correctFormat();
        }
        return cols;
    }

    /**
     * RBAC 권한에 따라 스프레드시트 전체를 읽기 전용으로 설정한다.
     * 사용자가 현재 타입에 대한 문서 편집 권한이 없을 경우 호출된다.
     *
     * @param readOnly true이면 모든 셀이 편집 불가
     */
    public void setReadOnly(boolean readOnly) {
        if (instance != null) {
            HandsontableConfig config = instance.getSettings();
            config.readOnly = readOnly;
            instance.updateSettings(config);
        }
    }

    private String[] toHeadersWithCheckbox(ColumnDef[] defs) {
        String[] headers = new String[defs.length + 1];
        headers[0] = ""; // checkbox column header (empty)
        for (int i = 0; i < defs.length; i++) {
            headers[i + 1] = defs[i].label();
        }
        return headers;
    }
}
