package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.Observable;
import jsinterop.annotations.JsFunction;

@JsFunction
public interface LanguageProvider {
    Observable<String> getLanguage();
}

