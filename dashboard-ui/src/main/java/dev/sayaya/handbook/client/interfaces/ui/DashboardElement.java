package dev.sayaya.handbook.client.interfaces.ui;

import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/** 대시보드 컨테이너. 통계 카드, 품질 패널, 에이전트 활동 로그를 조합한다. */
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
