package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.client.usecase.StatsProvider;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/** 통계 카드 3종 (타입 수, 문서 수, 사용자 수). MD3 Card 패턴. */
@Singleton
public class StatsCardElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;
    private final elemental2.dom.HTMLElement typeCountValue;
    private final elemental2.dom.HTMLElement docCountValue;
    private final elemental2.dom.HTMLElement userCountValue;

    @Inject
    public StatsCardElement(StatsProvider statsProvider) {
        typeCountValue = span().css("dash-stat-value").element();
        typeCountValue.textContent = "0";
        docCountValue = span().css("dash-stat-value").element();
        docCountValue.textContent = "0";
        userCountValue = span().css("dash-stat-value").element();
        userCountValue.textContent = "0";

        element = div().css("dash-stats-row")
                .add(div().css("dash-stat-card")
                        .add(typeCountValue)
                        .add(span().css("dash-stat-label").element()))
                .add(div().css("dash-stat-card")
                        .add(docCountValue)
                        .add(span().css("dash-stat-label").element()))
                .add(div().css("dash-stat-card")
                        .add(userCountValue)
                        .add(span().css("dash-stat-label").element()))
                .element();

        // 라벨 텍스트 설정
        var cards = element.querySelectorAll(".dash-stat-label");
        if (cards.length >= 3) {
            cards.getAt(0).textContent = "타입 수";
            cards.getAt(1).textContent = "문서 수";
            cards.getAt(2).textContent = "사용자 수";
        }

        statsProvider.subscribe(stats -> {
            if (stats != null) {
                typeCountValue.textContent = String.valueOf(stats.typeCount);
                docCountValue.textContent = String.valueOf(stats.documentCount);
                userCountValue.textContent = String.valueOf(stats.userCount);
            }
        });
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
