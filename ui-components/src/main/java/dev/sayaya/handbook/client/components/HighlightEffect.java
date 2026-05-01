package dev.sayaya.handbook.client.components;

import elemental2.core.JsArray;
import elemental2.dom.*;

/**
 * 범용 DOM 요소 강조 효과 컴포넌트.
 *
 * <p><b>책임:</b> {@code agent-command#highlight} 규약의 두 측면을 모두 담당한다.
 * <ul>
 *   <li><b>apply (발행):</b> CSS 선택자로 대상 요소를 찾아 {@code .ui-highlight} 클래스를
 *       부여/해제한다 ({@code agent-ui}/{@code assistant} 가 사용).</li>
 *   <li><b>observe (수신):</b> 특정 앵커 요소에 {@code .ui-highlight} 가 들어올 때 콜백을
 *       호출한다 — {@link MutationObserver} 를 랩핑해 소비자가 직접 DOM API 를 다루지
 *       않도록 한다 (D — Dependency Inversion). MenuRailItemElement / MobileTabsElement /
 *       ShellAppBarElement 등 다중 소비자가 동일 추상에 의존한다.</li>
 * </ul></p>
 *
 * <p><b>SOLID:</b>
 * <ul>
 *   <li>S: 강조 class 관리라는 단일 도메인. apply/observe 는 동일 규약(class 이름)의 양면.</li>
 *   <li>O: observe 는 Runnable 기반 콜백이라 신규 반응(코치마크·스크롤 등) 은 소비자 측
 *       콜백 교체로 확장 가능, 본체 수정 불필요.</li>
 *   <li>D: 소비자는 구체 MutationObserver 를 몰라도 된다.</li>
 * </ul></p>
 *
 * <p><b>의존관계:</b>
 * <ul><li>{@link elemental2.dom.DomGlobal#document} — apply 쿼리</li>
 * <li>{@link MutationObserver} — observe 감시</li></ul></p>
 */
public class HighlightEffect {
    /** 강조 class 이름. apply/observe 공통 규약. */
    public static final String CLASS_HIGHLIGHT = "ui-highlight";
    /** @deprecated 내부 호환을 위해 유지 — 새 코드는 {@link #CLASS_HIGHLIGHT} 를 사용. */
    @Deprecated private static final String HIGHLIGHT_CLASS = CLASS_HIGHLIGHT;
    private Element currentTarget;

    /** 대상 요소에 강조 효과를 적용한다. 이전 강조는 자동 해제된다. */
    public void highlight(String selector) {
        clear();
        if (selector == null) return;
        Element el = DomGlobal.document.querySelector(selector);
        if (el != null) {
            el.classList.add(HIGHLIGHT_CLASS);
            currentTarget = el;
        }
    }

    /** 현재 강조를 해제한다. */
    public void clear() {
        if (currentTarget != null) {
            currentTarget.classList.remove(HIGHLIGHT_CLASS);
            currentTarget = null;
        }
    }

    /**
     * {@code anchor} 의 class 변화를 감시해 {@code .ui-highlight} 가 부여될 때마다
     * {@code onHighlighted} 를 호출한다. {@link MutationObserver} 를 래핑하여 소비자는
     * DOM 관찰 API 의존에서 해방된다.
     *
     * @return 생성된 {@link MutationObserver} — 수동 해제가 필요한 경우 호출자가 보관.
     */
    public static MutationObserver observe(Element anchor, Runnable onHighlighted) {
        MutationObserver observer = new MutationObserver((JsArray<MutationRecord> records, MutationObserver o) -> {
            for (int i = 0; i < records.length; i++) {
                MutationRecord r = records.getAt(i);
                if ("class".equals(r.attributeName) && anchor.classList.contains(CLASS_HIGHLIGHT)) {
                    onHighlighted.run();
                }
            }
            return null;
        });
        MutationObserverInit init = MutationObserverInit.create();
        init.setAttributes(true);
        init.setAttributeFilter(JsArray.of("class"));
        observer.observe(anchor, init);
        return observer;
    }
}
