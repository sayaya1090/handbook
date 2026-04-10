package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.rx.Observable;

/**
 * 언어팩 JSON을 로드하는 포트 인터페이스.
 *
 * <p><b>책임:</b> 지정된 언어 코드에 해당하는 번역 레이블(Labels)을 비동기로 로드한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 포트 인터페이스, 각 모듈에서 FetchApi + AsyncSubject로 구현)</li></ul></p>
 */
public interface LanguagePackRepository {
    /** 지정된 언어의 번역 레이블을 로드한다. */
    Observable<Labels> load(String lang);
}
