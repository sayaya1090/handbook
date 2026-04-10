package dev.sayaya.handbook.usecase;

import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.rx.Observable;

/** 언어팩 JSON을 로드하는 포트. */
public interface LanguagePackRepository {
    /** 지정된 언어의 번역 레이블을 로드한다. */
    Observable<Labels> load(String lang);
}
