package dev.sayaya.handbook.client.canvas;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.api.TypeNative;
import dev.sayaya.handbook.client.domain.Type;
import elemental2.dom.DomGlobal;

import static elemental2.core.Global.JSON;
import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override public void onModuleLoad() {
        var canvas = components.canvas();
        body().add(components.controller()).add(canvas);

        String json = "{\n" +
                "  \"id\": \"exampleId\",\n" +
                "  \"version\": \"1.0\",\n" +
                "  \"effect_date_time\": 1672531200000,\n" +
                "  \"expire_date_time\": 1672534800000,\n" +
                "  \"description\": \"This is a test description\",\n" +
                "  \"primitive\": true,\n" +
                "  \"attributes\": [\n" +
                "    {\n" +
                "      \"name\": \"attr1\",\n" +
                "      \"details\": {\n" +
                "        \"key1\": \"value1\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"parent\": \"parentType\"\n" +
                "}";
        Type t =  ((TypeNative) JSON.parse(json)).toType();
        DomGlobal.console.log(t);
        DomGlobal.console.log(TypeNative.toJSON(t));
    }
}
