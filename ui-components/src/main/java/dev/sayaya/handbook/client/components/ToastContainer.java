package dev.sayaya.handbook.client.components;

import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.ui.elements.FeedbackElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import static dev.sayaya.ui.elements.FeedbackElementBuilder.toast;
import static org.jboss.elemento.Elements.div;

/**
 * 범용 토스트 메시지 컴포넌트. sayaya-ui {@code md-toast}({@link FeedbackElementBuilder#toast()}) 에 위임한다.
 *
 * <p><b>책임:</b> ToastLevel별 토스트를 화면 오른쪽 위 스택({@code .md-toast-stack}, sayaya-ui 가 body 에 자동 생성)에 쌓아 표시한다.
 * INFO/SUCCESS는 3초 후 자동으로 닫히고, WARNING/ERROR는 닫기 버튼으로 닫는다. 스택 최대 개수는 sayaya-ui 기본값(5)을 따른다.</p>
 * <p><b>의존관계:</b> <ul><li>{@link ToastLevel} — 토스트 심각도 열거형</li>
 * <li>sayaya-ui 2.5.0.2+ {@code toast()} 빌더와 js/sayaya-ui.js 번들의 {@code md-toast} 정의</li></ul></p>
 * <p><b>주의점:</b> {@link #element()} 는 기존 호출부(DI·body 추가) 호환을 위한 숨김 마커({@code .ui-toast-container})이며
 * 토스트는 그 안에 붙지 않는다. 각 토스트 host 에는 {@code ui-toast}, {@code ui-toast-<level>} 클래스가 붙고,
 * 닫기 버튼은 shadow 안 {@code [part=close]}, 퇴장 중 상태는 {@code [leaving]} 속성으로 접근한다.</p>
 */
public class ToastContainer implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    public ToastContainer() {
        root = div().css("ui-toast-container").style("display: none;").element();
    }

    /** 토스트를 표시한다. INFO/SUCCESS 는 3초 후 자동으로 닫힌다. */
    public void show(ToastLevel level, String message) {
        toast().level(toLevel(level)).text(message).show();
    }

    /** 지정 시간(ms) 후 자동으로 닫히는 토스트를 표시한다(레벨과 무관). */
    public void show(ToastLevel level, String message, int autoCloseMs) {
        toast().level(toLevel(level)).timeout(autoCloseMs).text(message).show();
    }

    private static FeedbackElementBuilder.ToastLevel toLevel(ToastLevel level) {
        switch (level) {
            case SUCCESS: return FeedbackElementBuilder.ToastLevel.Success;
            case WARNING: return FeedbackElementBuilder.ToastLevel.Warning;
            case ERROR: return FeedbackElementBuilder.ToastLevel.Error;
            default: return FeedbackElementBuilder.ToastLevel.Info;
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}
