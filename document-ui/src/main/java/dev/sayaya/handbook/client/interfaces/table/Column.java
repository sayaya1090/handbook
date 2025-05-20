package dev.sayaya.handbook.client.interfaces.table;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace= JsPackage.GLOBAL, name="Object")
@Accessors(fluent=true)
public final class Column {
    private String data;
    private String header;
    private String type;
    private String format;
    private String dateFormat;
    private Object source;
    private boolean strict;
    private boolean readOnly;
    //private Validator validator;
    private boolean allowInvalid;
    private boolean allowEmpty;
    private boolean filter;
}
