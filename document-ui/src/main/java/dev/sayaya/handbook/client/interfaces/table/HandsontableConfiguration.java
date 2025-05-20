package dev.sayaya.handbook.client.interfaces.table;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace= JsPackage.GLOBAL, name="Object")
@Accessors(fluent=true)
public class HandsontableConfiguration {
    public String[][] data;
    public String stretchH;
    public Object width;
    private Object height;
    private Integer minRows;
    private Integer maxRows;
    private Integer fixedRowsTop;
    private Integer fixedColumnsLeft;
    public Object rowHeaders;
    private Object rowHeaderWidth;
    private boolean manualRowResize;
    private boolean manualColumnResize;
    private boolean manualRowMove;
    private boolean manualColumnMove;
    private Boolean renderAllRows;
    private Double viewportColumnRenderingOffset;
    private Object contextMenu;
    private boolean autoRowSize;
    private boolean autoColSize;
    private Column[] columns;
    public Object colHeaders;
    private boolean formulas;
    private String preventOverflow;
    private boolean disableVisualSelection;
    // private Change beforeChange;
    private Object rowHeights;
    private Object colWidths;
    private MergeCell[] mergeCells;
}
