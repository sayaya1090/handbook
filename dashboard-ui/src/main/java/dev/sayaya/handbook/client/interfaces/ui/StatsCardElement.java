package dev.sayaya.handbook.client.interfaces.ui;

import dev.sayaya.handbook.client.usecase.StatsProvider;
import dev.sayaya.handbook.usecase.LabelProvider;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 워크스페이스 통계 카드 3종 (타입 수, 문서 수, 사용자 수) UI 요소.
 *
 * <p><b>책임:</b> StatsProvider를 구독하여 통계 값을 실시간 갱신하고, LabelProvider를 통해 라벨을 다국어 처리한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link StatsProvider} — 워크스페이스 통계 상태 구독</li>
 *   <li>{@link LabelProvider} — 카드 라벨 다국어 텍스트</li>
 * </ul></p>
 */
@Singleton
public class StatsCardElement implements IsElement<elemental2.dom.HTMLElement> {
    @Delegate private final HTMLContainerBuilder<elemental2.dom.HTMLDivElement> _this = div();
    private final elemental2.dom.HTMLElement typeCountValue;
    private final elemental2.dom.HTMLElement docCountValue;
    private final elemental2.dom.HTMLElement userCountValue;
    private final elemental2.dom.HTMLElement typeLabelEl;
    private final elemental2.dom.HTMLElement docLabelEl;
    private final elemental2.dom.HTMLElement userLabelEl;

    @Inject
    public StatsCardElement(StatsProvider statsProvider, LabelProvider labelProvider) {
        typeCountValue = span().css("dash-stat-value").element();
        typeCountValue.textContent = "0";
        docCountValue = span().css("dash-stat-value").element();
        docCountValue.textContent = "0";
        userCountValue = span().css("dash-stat-value").element();
        userCountValue.textContent = "0";

        typeLabelEl = span().css("dash-stat-label").element();
        docLabelEl = span().css("dash-stat-label").element();
        userLabelEl = span().css("dash-stat-label").element();

        _this.css("dash-stats-row")
                .add(div().css("dash-stat-card")
                        .add(typeCountValue)
                        .add(typeLabelEl))
                .add(div().css("dash-stat-card")
                        .add(docCountValue)
                        .add(docLabelEl))
                .add(div().css("dash-stat-card")
                        .add(userCountValue)
                        .add(userLabelEl));

        labelProvider.subscribe(labels -> {
            typeLabelEl.textContent = labels.getOrDefault("dashboard.stats.types", "Types");
            docLabelEl.textContent = labels.getOrDefault("dashboard.stats.documents", "Documents");
            userLabelEl.textContent = labels.getOrDefault("dashboard.stats.users", "Users");
        });

        statsProvider.subscribe(stats -> {
            if (stats != null) {
                typeCountValue.textContent = String.valueOf(stats.typeCount);
                docCountValue.textContent = String.valueOf(stats.documentCount);
                userCountValue.textContent = String.valueOf(stats.userCount);
            }
        });
    }
}
