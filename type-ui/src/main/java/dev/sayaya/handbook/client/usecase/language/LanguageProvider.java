package dev.sayaya.handbook.client.usecase.language;

import dev.sayaya.rx.Observable;

public interface LanguageProvider {
    Observable<String> getLanguage();
}

