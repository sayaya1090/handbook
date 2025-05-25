package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.interfaces.table.function.AfterGetColumnHeaderRenderers;
import dev.sayaya.handbook.client.interfaces.table.function.AfterGetRowHeaderRenderers;
import dev.sayaya.handbook.client.interfaces.table.function.MouseEventHandler;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.experimental.Accessors;

import java.util.Objects;

@JsType(isNative = true, namespace= JsPackage.GLOBAL, name="Object")
@Accessors(fluent=true)
public final class HandsontableConfiguration {
    public Data[] data;
    public String stretchH;
    public Object width;
    public Object height;
    public Integer minRows;
    public Integer maxRows;
    public Integer fixedRowsTop;
    public Integer fixedColumnsLeft;
    public Object rowHeaders;
    public Object rowHeaderWidth;
    public boolean manualRowResize;
    public boolean manualColumnResize;
    public boolean manualRowMove;
    public boolean manualColumnMove;
    public Boolean renderAllRows;
    public Double viewportColumnRenderingOffset;
    public Object contextMenu;
    public boolean autoRowSize;
    public boolean autoColSize;
    public Column[] columns;
    public Object colHeaders;
    public boolean formulas;
    public String preventOverflow;
    public boolean disableVisualSelection;
    // public Change beforeChange;
    public Object rowHeights;
    public Object colWidths;
    public MergeCell[] mergeCells;
    public AfterGetColumnHeaderRenderers afterGetColumnHeaderRenderers;
    public AfterGetRowHeaderRenderers afterGetRowHeaderRenderers;
    public MouseEventHandler afterOnCellMouseDown;
    public MouseEventHandler afterOnCellMouseOver;
    public MouseEventHandler afterOnCellMouseUp;
    public MouseEventHandler afterOnCellContextMenu;
}
