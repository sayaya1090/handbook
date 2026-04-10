package dev.sayaya.handbook.usecase;

/**
 * 현재 사용자의 언어 코드를 감지하는 포트 인터페이스.
 *
 * <p><b>책임:</b> localStorage 또는 navigator.language에서 사용자 언어 코드(예: "ko", "en")를 감지한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 포트 인터페이스, 각 모듈에서 JSNI로 구현)</li></ul></p>
 */
public interface LanguageDetector {
    /** 언어 코드를 반환한다 (예: "ko", "en"). */
    String detect();
}
