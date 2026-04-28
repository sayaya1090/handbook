package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * window 객체를 통해 GWT 모듈 간 다국어 레이블(Labels) 정보를 공유하는 브릿지.
 * 
 * <p>window 속성: {@code __handbook_labels}</p>
 */
public final class LabelSharing {
    private static final String KEY = "__handbook_labels";
    private static final BehaviorSubject<Labels> labelsSubject = BehaviorSubject.behavior(null);

    private LabelSharing() {}

    /** shell-ui 측: 로드된 Labels 객체를 공유한다. */
    public static void publish(Labels labels) {
        Js.asPropertyMap(DomGlobal.window).set(KEY, labels);
        labelsSubject.next(labels);
    }

    /** 자식 모듈 측: 공유된 Labels 객체를 수신할 콜백을 등록한다. */
    public static void register(LabelFn callback) {
        JsPropertyMap<Object> win = Js.asPropertyMap(DomGlobal.window);
        if (win.has(KEY)) {
            callback.call(Js.cast(win.get(KEY)));
        }
    }

    @JsFunction
    public interface LabelFn {
        void call(Labels labels);
    }
}
