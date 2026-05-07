package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 특정 타입의 모든 버전을 타임라인으로 표시하는 히스토리 패널.
 *
 * <p><b>책임:</b> TypeRepository.versions()를 호출하여 타입의 모든 버전을 조회하고,
 * 각 버전의 version 문자열, effectDateTime, 속성 수를 목록으로 렌더링한다.
 * 두 버전을 클릭하면 diff API를 호출하여 차이를 표시한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link TypeRepository} — 버전 목록 조회 및 diff API 호출</li>
 *   <li>{@link LabelProvider} — 패널 제목 및 빈 상태 메시지 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 싱글턴이므로 동시에 하나의 패널만 표시된다.
 * show() 호출 시 대상 typeId를 설정하고 버전을 로드한다.
 * Escape 키 또는 닫기 버튼으로 패널을 닫을 수 있다.</p>
 */
@Singleton
public class VersionHistoryPanel implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLDivElement headerDiv;
    private final HTMLDivElement listContainer;
    private final HTMLDivElement diffContainer;
    private final TypeRepository typeRepository;
    private Labels labels = Labels.empty();
    private String currentTypeId;
    private final List<String> selectedVersions = new ArrayList<>();

    @Inject
    VersionHistoryPanel(TypeRepository typeRepository, LabelProvider labelProvider) {
        this.typeRepository = typeRepository;

        headerDiv = div().css("version-history-header").element();
        headerDiv.textContent = "Version History";

        HTMLElement closeBtn = (HTMLElement) DomGlobal.document.createElement("button");
        closeBtn.classList.add("version-history-close");
        closeBtn.textContent = "\u2715";
        closeBtn.addEventListener("click", e -> hide());

        var headerRow = div().css("version-history-header-row").element();
        headerRow.appendChild(headerDiv);
        headerRow.appendChild(closeBtn);

        listContainer = div().css("version-history-list").element();
        // 이벤트 위임: 개별 row가 아닌 listContainer에서 클릭 처리
        listContainer.addEventListener("click", e -> {
            HTMLElement target = (HTMLElement) e.target;
            HTMLElement row = findAncestorWithClass(target, "version-history-row");
            if (row == null) return;
            HTMLElement versionEl = (HTMLElement) row.querySelector(".version-history-version");
            if (versionEl != null) toggleVersionSelection(versionEl.textContent, row);
        });
        diffContainer = div().css("version-history-diff").element();

        root = div().css("version-history-panel")
                .add(headerRow)
                .add(listContainer)
                .add(diffContainer)
                .element();
        root.style.setProperty("display", "none");

        root.addEventListener("keydown", evt -> {
            elemental2.dom.KeyboardEvent ke = (elemental2.dom.KeyboardEvent) evt;
            if ("Escape".equals(ke.key)) hide();
        });

        labelProvider.subscribe(l -> {
            this.labels = l;
            headerDiv.textContent = l.getOrDefault("type.version_history.title", "Version History");
            closeBtn.setAttribute("aria-label", l.getOrDefault("type.version_history.close", "Close"));
        });
    }

    public void show(String typeId) {
        this.currentTypeId = typeId;
        selectedVersions.clear();
        diffContainer.innerHTML = "";
        listContainer.innerHTML = "";

        var loading = div().css("version-history-loading").element();
        loading.textContent = labels.getOrDefault("type.version_history.loading", "Loading...");
        listContainer.appendChild(loading);

        root.style.setProperty("display", "flex");
        root.setAttribute("tabindex", "0");
        root.focus();

        typeRepository.versions(typeId).subscribe(versions -> renderVersions(versions));
    }

    public void hide() {
        root.style.setProperty("display", "none");
        currentTypeId = null;
        selectedVersions.clear();
    }

    private void renderVersions(Set<Type> versions) {
        listContainer.innerHTML = "";
        if (versions == null || versions.isEmpty()) {
            var empty = div().css("version-history-empty").element();
            empty.textContent = labels.getOrDefault("type.version_history.empty", "No versions found");
            listContainer.appendChild(empty);
            return;
        }

        var hint = div().css("version-history-hint").element();
        hint.textContent = labels.getOrDefault("type.version_history.diff_hint",
                "Click two versions to compare");
        listContainer.appendChild(hint);

        for (Type version : versions) {
            var versionLabel = span().css("version-history-version").element();
            versionLabel.textContent = version.version();

            var effectDate = span().css("version-history-date").element();
            effectDate.textContent = formatTimestamp(version.effectDateTime());

            var attrCount = span().css("version-history-attrs").element();
            int count = version.attributes() != null ? version.attributes().length : 0;
            attrCount.textContent = count + " " +
                    labels.getOrDefault("type.version_history.attributes", "attributes");

            var row = div().css("version-history-row").element();
            row.appendChild(versionLabel);
            row.appendChild(effectDate);
            row.appendChild(attrCount);

            // 클릭 이벤트는 listContainer에서 위임 처리
            listContainer.appendChild(row);
        }
    }

    private void toggleVersionSelection(String version, HTMLElement row) {
        if (selectedVersions.contains(version)) {
            selectedVersions.remove(version);
            row.removeAttribute("selected");
        } else {
            if (selectedVersions.size() >= 2) {
                // 2개 이상 선택 시 첫 번째를 제거
                selectedVersions.remove(0);
                // 모든 row에서 selected 해제 후 현재 선택된 것만 재적용
                elemental2.dom.NodeList<elemental2.dom.Element> rows =
                        listContainer.querySelectorAll(".version-history-row");
                for (int i = 0; i < rows.length; i++) {
                    HTMLElement r = (HTMLElement) rows.getAt(i);
                    String v = r.querySelector(".version-history-version").textContent;
                    if (selectedVersions.contains(v)) r.setAttribute("selected", "");
                    else r.removeAttribute("selected");
                }
            }
            selectedVersions.add(version);
            row.setAttribute("selected", "");
        }

        if (selectedVersions.size() == 2) {
            loadDiff(selectedVersions.get(0), selectedVersions.get(1));
        } else {
            diffContainer.innerHTML = "";
        }
    }

    private void loadDiff(String v1, String v2) {
        diffContainer.innerHTML = "";
        var loading = div().css("version-history-diff-loading").element();
        loading.textContent = labels.getOrDefault("type.version_history.loading_diff", "Loading diff...");
        diffContainer.appendChild(loading);

        // diff API 호출은 기존 TypeController.diff 엔드포인트를 활용
        fetchDiff(currentTypeId, v1, v2);
    }

    /**
     * diff API를 호출하여 두 버전 간 차이를 가져온다.
     * 응답 JSON을 renderDiff()에 전달하고, 실패 시 renderDiffError()를 호출한다.
     */
    private void fetchDiff(String typeId, String v1, String v2) {
        String workspace = jsinterop.base.Js.asPropertyMap(DomGlobal.window).getAsAny("__handbook_workspace").asString();
        String url = "workspace/" + workspace + "/types/" + elemental2.core.Global.encodeURIComponent(typeId)
                + "/diff?v1=" + elemental2.core.Global.encodeURIComponent(v1) + "&v2=" + elemental2.core.Global.encodeURIComponent(v2);
        elemental2.dom.RequestInit init = elemental2.dom.RequestInit.create();
        init.setCredentials("same-origin");
        DomGlobal.fetch(url, init)
                .then(r -> r.json())
                .then(diff -> { renderDiff(diff); return null; })
                .catch_(err -> { renderDiffError("" + err); return null; });
    }

    private void renderDiff(Object diffObj) {
        diffContainer.innerHTML = "";
        if (diffObj == null) {
            renderDiffError("No diff data");
            return;
        }

        jsinterop.base.JsPropertyMap<?> diff = jsinterop.base.Js.cast(diffObj);
        var title = div().css("version-history-diff-title").element();
        title.textContent = labels.getOrDefault("type.version_history.diff_title", "Changes") +
                " (" + selectedVersions.get(0) + " \u2192 " + selectedVersions.get(1) + ")";
        diffContainer.appendChild(title);

        renderDiffSection(diff, "changes", "version-history-diff-change",
                labels.getOrDefault("type.version_history.changed", "Changed"));
        renderDiffSection(diff, "added", "version-history-diff-added",
                labels.getOrDefault("type.version_history.added", "Added"));
        renderDiffSection(diff, "removed", "version-history-diff-removed",
                labels.getOrDefault("type.version_history.removed", "Removed"));
    }

    private void renderDiffSection(jsinterop.base.JsPropertyMap<?> diff, String key,
                                    String cssClass, String sectionLabel) {
        Object arr = diff.get(key);
        if (arr == null) return;
        elemental2.core.JsArray<String> items = jsinterop.base.Js.cast(arr);
        if (items.length == 0) return;

        var section = div().css("version-history-diff-section").element();
        var label = span().css("version-history-diff-label").element();
        label.textContent = sectionLabel + ":";
        section.appendChild(label);

        for (int i = 0; i < items.length; i++) {
            var item = div().css(cssClass).element();
            item.textContent = items.getAt(i);
            section.appendChild(item);
        }
        diffContainer.appendChild(section);
    }

    private void renderDiffError(String error) {
        diffContainer.innerHTML = "";
        var errDiv = div().css("version-history-diff-error").element();
        errDiv.textContent = labels.getOrDefault("type.version_history.diff_error", "Diff failed") + ": " + error;
        diffContainer.appendChild(errDiv);
    }

    /** 상위 요소 중 해당 CSS 클래스를 가진 요소를 찾는다 */
    private static HTMLElement findAncestorWithClass(HTMLElement el, String className) {
        while (el != null) {
            if (el.classList.contains(className)) return el;
            el = (HTMLElement) el.parentElement;
        }
        return null;
    }

    /** 타임스탬프(ms)를 "yyyy-MM-dd" 형식으로 변환한다. */
    private static String formatTimestamp(double ts) {
        elemental2.core.JsDate d = new elemental2.core.JsDate(ts);
        int y = (int) d.getFullYear();
        int mon = (int) d.getMonth() + 1;
        int day = (int) d.getDate();
        return y + "-"
             + (mon < 10 ? "0" + mon : "" + mon) + "-"
             + (day < 10 ? "0" + day : "" + day);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
