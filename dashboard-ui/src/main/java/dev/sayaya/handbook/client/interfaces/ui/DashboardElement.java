package dev.sayaya.handbook.client.interfaces.ui;

import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 대시보드 메인 컨테이너 UI 요소.
 *
 * <p><b>책임:</b> StatsCardElement, QualityPanelElement, ActivityLogElement를 조합하여 대시보드 레이아웃을 구성한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link StatsCardElement} — 타입/문서/사용자 수 통계 카드</li>
 *   <li>{@link QualityPanelElement} — 품질 이슈 목록 패널</li>
 *   <li>{@link ActivityLogElement} — 에이전트 활동 타임라인</li>
 * </ul></p>
 */
@Singleton
public class DashboardElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;

    @Inject
    public DashboardElement(StatsCardElement statsCard, QualityPanelElement qualityPanel, ActivityLogElement activityLog) {
        element = div().css("dash-container")
                .add(statsCard)
                .add(div().css("dash-panels")
                        .add(qualityPanel)
                        .add(activityLog))
                .element();
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
