package dev.sayaya.handbook.usecase;

/** 현재 사용자의 언어 코드를 감지하는 포트. */
public interface LanguageDetector {
    /** 언어 코드를 반환한다 (예: "ko", "en"). */
    String detect();
}
