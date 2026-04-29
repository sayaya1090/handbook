package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.usecase.SessionEnvironment;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
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
    public Double getJwtClaimAsDouble(String token, String claim) {
        try {
            String[] jwtParts = token.split("\\.");
            if (jwtParts.length >= 2) {
                String decoded = Js.<AtobWindow>cast(DomGlobal.window).atob(jwtParts[1]);
                Object parsed = elemental2.core.Global.JSON.parse(decoded);
                JsPropertyMap<?> map = Js.cast(parsed);
                jsinterop.base.Any val = (jsinterop.base.Any) map.get(claim);
                if (val != null) return val.asDouble();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
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

