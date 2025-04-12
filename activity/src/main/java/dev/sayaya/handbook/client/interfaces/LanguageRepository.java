package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.LanguageProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.DomGlobal;
import elemental2.webstorage.StorageEvent;
import elemental2.webstorage.WebStorageWindow;
import jsinterop.base.Js;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static elemental2.dom.DomGlobal.*;

@Singleton
public final class LanguageRepository implements LanguageProvider {
    @Delegate private final Subject<String> language = behavior(null);
    @Inject LanguageRepository() {
        syncFromLocalStorage();
        syncFromNavigator();
        language.distinct().subscribe(newLang -> {          // language 변경 시 LocalStorage에 기록
            if (newLang != null && !newLang.isEmpty()) WebStorageWindow.of(window).localStorage.setItem("lang", newLang);
        });

        String lang = readFromLocalStorage();
        if(lang==null || lang.isEmpty()) lang = readFromNavigator();
        language.next(lang);
    }
    private void syncFromLocalStorage() {
        DomGlobal.window.addEventListener("storage", evt -> {
            StorageEvent storageEvent = Js.cast(evt);
            if (!"lang".equals(storageEvent.key)) return;
            String lang = readFromLocalStorage();
            if(lang!=null && !lang.isEmpty()) language.next(lang);
        });
    }
    private String readFromLocalStorage() {
        return WebStorageWindow.of(window).localStorage.getItem("lang");
    }
    private void syncFromNavigator() {
        DomGlobal.window.addEventListener("visibilitychange", evt -> {
            if (document.hidden) return;
            language.next(readFromNavigator());
        });
    }
    private String readFromNavigator() {
        var lang = navigator.language != null ? navigator.language : "en";
        if (lang.contains("-")) lang = lang.split("-")[0];
        return lang;
    }

    @Override
    public Observable<String> getLanguage() {
        return language;
    }
}
