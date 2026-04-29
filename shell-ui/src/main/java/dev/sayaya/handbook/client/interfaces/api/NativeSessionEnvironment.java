package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.usecase.SessionEnvironment;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NativeSessionEnvironment implements SessionEnvironment {
    @Inject
    public NativeSessionEnvironment() {}

    @Override
    public String getCookies() {
        return ((elemental2.dom.HTMLDocument) DomGlobal.document).cookie;
    }

    @Override
    public String decodeBase64(String encoded) {
        return Js.<AtobWindow>cast(DomGlobal.window).atob(encoded);
    }

    @Override
    public Object parseJson(String json) {
        return elemental2.core.Global.JSON.parse(json);
    }

    @Override
    public void redirect(String path) {
        DomGlobal.window.location.assign(path);
    }

    @Override
    public void clearInterval(double handle) {
        DomGlobal.clearInterval(handle);
    }

    @jsinterop.annotations.JsType(isNative = true, namespace = jsinterop.annotations.JsPackage.GLOBAL, name = "Window")
    private static class AtobWindow {
        public native String atob(String encoded);
    }
}
