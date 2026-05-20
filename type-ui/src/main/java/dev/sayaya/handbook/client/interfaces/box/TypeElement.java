package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.editor.AttributeEditorDialog;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.interfaces.value.ValueListElement;
import dev.sayaya.handbook.client.usecase.CanvasMode;
import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.EditBoxAction;
import dev.sayaya.handbook.client.usecase.action.ResizeBoxAction;
import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

/**
 * 캔버스 위의 타입 카드 요소(MD3 Card 스타일).
 *
 * <p><b>책임:</b> 타입 이름/버전 표시, 속성 목록 렌더링, 드래그 선택,
 * 인라인 이름/버전 편집, 속성 편집/삭제, 리사이즈 핸들을 지원한다.
 * PositionMap을 구독하여 위치 변경 시 자동 반영하고,
 * SelectedBoxElement을 구독하여 선택 시각 상태를 토글한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link PositionMap} — 위치 구독 및 리사이즈 반영</li>
 *   <li>{@link SelectedBoxElement} — 선택 상태 구독/토글</li>
 *   <li>{@link ActionManager} — EditBoxAction/ResizeBoxAction 실행</li>
 *   <li>{@link TypeList} — 타입 갱신</li>
 *   <li>{@link ChangeTracker} — 변경 마킹</li>
 *   <li>{@link AttributeEditorDialog} — 속성 편집 다이얼로그</li>
 *   <li>{@link CanvasMode} — LAYOUT/TYPE 모드에 따른 동작 분기</li>
 *   <li>{@link GridSnap} — 리사이즈 시 스냅 적용</li>
 *   <li>{@link ValueListElement} — 속성 목록 렌더링</li>
 * </ul></p>
 * <p><b>주의:</b> Dagger AssistedInject로 생성된다(Type, Position이 Assisted 파라미터).
 * TYPE 모드에서 이름/버전 더블클릭 시 인라인 input 편집이 시작된다.</p>
 */
public class TypeElement implements IsElement<HTMLDivElement> {
    public enum DisplayMode { SIMPLE, DETAIL }

    private final HTMLDivElement root;
    private final HTMLDivElement nameLabel;
    private final HTMLDivElement versionLabel;
    private final ValueListElement valueList;
    private final ActionManager actionManager;
    private final TypeList typeList;
    private final ChangeTracker tracker;
    private final AttributeEditorDialog editorDialog;
    private final GridSnap gridSnap;
    private final CanvasMode canvasMode;
    private Type type;
    private final PositionMap positionMap;
    private final SelectedBoxElement selection;
    private final LayoutProvider layoutProvider;
    private final dev.sayaya.handbook.client.usecase.IntegrityAnalysisService integrityService;
    private final dev.sayaya.handbook.client.interfaces.editor.ConflictResolutionDialog resolutionDialog;
    private DisplayMode displayMode = DisplayMode.DETAIL;

    private boolean dragging = false;
    private int dragStartX, dragStartY;

    @AssistedInject
    TypeElement(@Assisted Type type, @Assisted Position position,
                PositionMap positionMap, SelectedBoxElement selection,
                ActionManager actionManager, TypeList typeList, ChangeTracker tracker,
                AttributeEditorDialog editorDialog, GridSnap gridSnap, CanvasMode canvasMode,
                LayoutProvider layoutProvider,
                dev.sayaya.handbook.client.usecase.IntegrityAnalysisService integrityService,
                dev.sayaya.handbook.client.interfaces.editor.ConflictResolutionDialog resolutionDialog) {
        this.type = type;
        this.positionMap = positionMap;
        this.selection = selection;
        this.actionManager = actionManager;
        this.typeList = typeList;
        this.tracker = tracker;
        this.editorDialog = editorDialog;
        this.gridSnap = gridSnap;
        this.canvasMode = canvasMode;
        this.layoutProvider = layoutProvider;
        this.integrityService = integrityService;
        this.resolutionDialog = resolutionDialog;


        nameLabel = div().css("type-name").element();
        nameLabel.textContent = type.id();
        nameLabel.addEventListener("dblclick", e -> {
            e.stopPropagation();
            canvasMode.getCurrentState().onNameDblClick(e, this::startInlineEdit);
        });

        versionLabel = div().css("type-version").element();
        versionLabel.textContent = type.version();
        versionLabel.addEventListener("dblclick", e -> {
            e.stopPropagation();
            canvasMode.getCurrentState().onVersionDblClick(e, this::startVersionEdit);
        });

        valueList = new ValueListElement();
        valueList.setOnEdit(this::editAttribute);
        valueList.setOnDelete(this::deleteAttribute);

        HTMLContainerBuilder<HTMLDivElement> header = div().css("type-header")
                .add(nameLabel)
                .add(versionLabel);

        HTMLElement resizeHandle = (HTMLElement) DomGlobal.document.createElement("div");
        resizeHandle.classList.add("type-resize-handle");
        initResizeHandle(resizeHandle);

        root = div().css("type-box")
                .attr("tabindex", "0")
                .attr("data-type-key", type.key())
                .add(header)
                .add(valueList)
                .element();
        root.appendChild(resizeHandle);

        applyPosition(position);
        setType(type);
        initEventHandlers();

        positionMap.subscribe(map -> {
            Position p = map.get(type.key());
            if (p != null) {
                applyPosition(p);
                updateChangedStyle();
            }
        });

        selection.subscribe(selected -> {
            if (selected.contains(type.key())) root.setAttribute("selected", "");
            else root.removeAttribute("selected");
        });
    }

    private void applyPosition(Position p) {
        root.style.setProperty("left", p.x() + "px");
        root.style.setProperty("top", p.y() + "px");
        root.style.setProperty("width", p.width() + "px");
        root.style.setProperty("height", p.height() + "px");
    }

    private void updateAttributes() {
        valueList.update(type.key(), type.attributes(), tracker);
    }

    private void initEventHandlers() {
        root.addEventListener("mousedown", e -> {
            elemental2.dom.MouseEvent me = (elemental2.dom.MouseEvent) e;
            if (me.button != 0) return;
            if (me.ctrlKey) selection.toggle(type.key());
            else if (!selection.isSelected(type.key())) selection.select(type.key());
            dragging = true;
            dragStartX = (int) me.clientX;
            dragStartY = (int) me.clientY;
            e.stopPropagation();
        });

        root.addEventListener("click", e -> e.stopPropagation());
    }

    // ── 인라인 이름 편집 ──
    private void startInlineEdit() {
        HTMLInputElement input = (HTMLInputElement) DomGlobal.document.createElement("input");
        input.classList.add("type-name-input");
        input.value = type.id();
        nameLabel.textContent = "";
        nameLabel.appendChild(input);
        input.focus();
        input.select();

        Runnable commit = () -> {
            String newName = input.value.trim();
            if (!newName.isEmpty() && !newName.equals(type.id())) {
                Type after = Type.create(newName, type.version(), type.effectDateTime(), type.expireDateTime());
                after.description(type.description());
                after.primitive(type.primitive());
                after.parent(type.parent());
                after.attributes(type.attributes());
                actionManager.execute(new EditBoxAction(typeList, positionMap, tracker, layoutProvider, integrityService, resolutionDialog, type, after));
            }
            nameLabel.textContent = type.id();
        };

        input.addEventListener("blur", e -> commit.run());
        input.addEventListener("keydown", e -> {
            elemental2.dom.KeyboardEvent ke = (elemental2.dom.KeyboardEvent) e;
            if ("Enter".equals(ke.key)) input.blur();
            else if ("Escape".equals(ke.key)) { input.value = type.id(); input.blur(); }
        });
    }

    // ── 인라인 버전 편집 ──
    private void startVersionEdit() {
        HTMLInputElement input = (HTMLInputElement) DomGlobal.document.createElement("input");
        input.classList.add("type-version-input");
        input.value = type.version();
        versionLabel.textContent = "";
        versionLabel.appendChild(input);
        input.focus();
        input.select();

        Runnable commit = () -> {
            String newVersion = input.value.trim();
            if (!newVersion.isEmpty() && !newVersion.equals(type.version())) {
                Type after = Type.create(type.id(), newVersion, type.effectDateTime(), type.expireDateTime());
                after.description(type.description());
                after.primitive(type.primitive());
                after.parent(type.parent());
                after.attributes(type.attributes());
                actionManager.execute(new EditBoxAction(typeList, positionMap, tracker, layoutProvider, integrityService, resolutionDialog, type, after));
            }
            versionLabel.textContent = type.version();
        };

        input.addEventListener("blur", e -> commit.run());
        input.addEventListener("keydown", e -> {
            elemental2.dom.KeyboardEvent ke = (elemental2.dom.KeyboardEvent) e;
            if ("Enter".equals(ke.key)) input.blur();
            else if ("Escape".equals(ke.key)) { input.value = type.version(); input.blur(); }
        });
    }

    // ── 속성 편집 ──
    private void editAttribute(Attribute attr) {
        editorDialog.show(type, attr, applied -> {
            Type before = type;
            Attribute[] oldAttrs = before.attributes();
            Attribute[] newAttrs = new Attribute[oldAttrs.length];
            for (int i = 0; i < oldAttrs.length; i++) {
                Attribute a = oldAttrs[i];
                if (a.name().equals(attr.name()) && a.order() == attr.order()) {
                    newAttrs[i] = applied;
                } else {
                    newAttrs[i] = a;
                }
            }
            Type after = before.withAttributes(newAttrs);
            actionManager.execute(new EditBoxAction(typeList, positionMap, tracker, layoutProvider, integrityService, resolutionDialog, before, after));
        });
    }

    // ── 속성 삭제 ──
    private void deleteAttribute(Attribute attr) {
        Type before = type;
        Attribute[] oldAttrs = before.attributes();
        java.util.List<Attribute> list = new java.util.ArrayList<>();
        for (Attribute a : oldAttrs) {
            if (!(a.name().equals(attr.name()) && a.order() == attr.order())) {
                list.add(a);
            }
        }
        Attribute[] newAttrs = list.toArray(new Attribute[0]);
        Type after = before.withAttributes(newAttrs);
        actionManager.execute(new EditBoxAction(typeList, positionMap, tracker, layoutProvider, integrityService, resolutionDialog, before, after));
    }

    // ── 리사이즈 핸들 ──
    private void initResizeHandle(HTMLElement handle) {
        final int[] startXY = new int[2];
        final Position[] startPos = new Position[1];

        handle.addEventListener("mousedown", e -> {
            e.stopPropagation();
            e.preventDefault();
            elemental2.dom.MouseEvent me = (elemental2.dom.MouseEvent) e;
            canvasMode.getCurrentState().onResizeMouseDown(me, () -> {
                startXY[0] = (int) me.clientX;
                startXY[1] = (int) me.clientY;
                startPos[0] = positionMap.get(type.key());
                if (startPos[0] == null) return;

                elemental2.dom.EventListener moveListener = new elemental2.dom.EventListener() {
                    @Override public void handleEvent(elemental2.dom.Event evt) {
                        elemental2.dom.MouseEvent mv = (elemental2.dom.MouseEvent) evt;
                        int dx = (int) mv.clientX - startXY[0];
                        int dy = (int) mv.clientY - startXY[1];
                        int newW = Math.max(120, gridSnap.snap(startPos[0].width() + dx));
                        int newH = Math.max(60, gridSnap.snap(startPos[0].height() + dy));
                        positionMap.put(type.key(), Position.of(startPos[0].x(), startPos[0].y(), newW, newH));
                    }
                };

                elemental2.dom.EventListener upListener = new elemental2.dom.EventListener() {
                    @Override public void handleEvent(elemental2.dom.Event evt) {
                        DomGlobal.document.removeEventListener("mousemove", moveListener);
                        DomGlobal.document.removeEventListener("mouseup", this);
                        Position endPos = positionMap.get(type.key());
                        if (endPos != null && startPos[0] != null &&
                                (endPos.width() != startPos[0].width() || endPos.height() != startPos[0].height())) {
                            // 이미 적용됨. undo용 액션만 기록.
                            Position before = startPos[0];
                            Position after = endPos;
                            actionManager.execute(new ResizeBoxAction(positionMap, layoutProvider, tracker, type.key(), before, after) {
                                @Override public void execute() {} // 이미 리사이즈 처리됨
                            });
                        }
                    }
                };

                DomGlobal.document.addEventListener("mousemove", moveListener);
                DomGlobal.document.addEventListener("mouseup", upListener);
            });
        });
    }

    public Type getType() { return type; }

    public void setType(Type type) {
        this.type = type;
        updateChangedStyle();
        nameLabel.textContent = type.id();
        nameLabel.classList.toggle("changed", tracker.getState(type.key() + ":id") == ChangeTracker.ChangeState.CHANGED);
        versionLabel.textContent = type.version();
        versionLabel.classList.toggle("changed", tracker.getState(type.key() + ":version") == ChangeTracker.ChangeState.CHANGED);
        updateAttributes();
    }

    private void updateChangedStyle() {
        boolean boxChanged = tracker.getState(type.key()) == ChangeTracker.ChangeState.CHANGED
                          || tracker.getState(type.key() + ":position") == ChangeTracker.ChangeState.CHANGED;
        root.classList.toggle("changed", boxChanged);
    }

    public DisplayMode getDisplayMode() { return displayMode; }

    public void setDisplayMode(DisplayMode mode) {
        this.displayMode = mode;
        if (mode == DisplayMode.SIMPLE) {
            valueList.element().style.setProperty("display", "none");
        } else {
            valueList.element().style.setProperty("display", "block");
        }
    }

    public int getDragStartX() { return dragStartX; }
    public int getDragStartY() { return dragStartY; }
    public boolean isDragging() { return dragging; }
    public void setDragging(boolean dragging) { this.dragging = dragging; }

    @Override
    public HTMLDivElement element() { return root; }
}
