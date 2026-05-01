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
 *
 * <p><b>책임:</b> 브라우저 언어를 감지하고 언어팩을 로드하여 {@link dev.sayaya.handbook.domain.Labels}
 * 를 BehaviorSubject로 발행한다. UI 컴포넌트의 다국어 텍스트 공급원 역할을 수행한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link LanguageDetector} — 현재 브라우저 언어 감지</li>
 *   <li>{@link LanguagePackRepository} — 서버/로컬에서 언어 JSON 로드</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 로드 실패 시 Labels.empty()를 유지하므로, 번역이 없는 경우 키(key)가 그대로 노출된다.</p>
 */
@Singleton
public class LabelProvider {
    private final BehaviorSubject<Labels> subject = behavior(Labels.empty());

    @Inject
    public LabelProvider(LanguageDetector detector, LanguagePackRepository repo) {
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
