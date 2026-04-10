package dev.sayaya.handbook.usecase;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.rx.Subscription;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 다국어 레이블을 제공하는 싱글톤 usecase.
 * 브라우저 언어를 감지하고 언어팩을 로드하여 BehaviorSubject로 발행한다.
 * 로드 실패 시 Labels.empty()를 유지한다 (키를 그대로 표시).
 */
@Singleton
public class LabelProvider {
    private final BehaviorSubject<Labels> subject = behavior(Labels.empty());

    @Inject
    LabelProvider(LanguageDetector detector, LanguagePackRepository repo) {
        String lang = detector.detect();
        try {
            repo.load(lang).subscribe(subject::next);
        } catch (Exception e) {
            GWT.log("Language pack load failed for '" + lang + "': " + e.getMessage());
        }
    }

    /** 레이블 변경을 구독한다. 초기값이 즉시 발행된다. */
    public Subscription subscribe(Consumer<Labels> consumer) {
        return subject.subscribe(consumer::accept);
    }
}
