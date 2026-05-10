package dev.sayaya.handbook.client.onboarding;

import javax.inject.Inject;

/**
 * SectionElement 구성을 위한 빌더 패턴 클래스.
 *
 * <p><b>역할:</b> SectionElement가 필요로 하는 다국어 라벨, 보조 설명, 플레이스홀더 및 CSS 클래스를
 * 빌더 체이닝을 통해 유연하게 설정하고 생성한다.</p>
 *
 * <p><b>사용법:</b>
 * <pre>
 * builder.cssClass("ws-section-create")
 *        .label("Create")
 *        .placeholder("Name")
 *        .build(Mode.CREATE.name());
 * </pre></p>
 */
public class SectionBuilder {
    private final SectionElement.Factory factory;
    private final SectionElementPresenter.Factory presenterFactory;
    private String label;
    private String supportingText;
    private String placeholder;
    private String cssClass;

    @Inject
    public SectionBuilder(SectionElement.Factory factory, SectionElementPresenter.Factory presenterFactory) {
        this.factory = factory;
        this.presenterFactory = presenterFactory;
    }

    /** 섹션의 타이틀 라벨을 설정한다. */
    public SectionBuilder label(String label) { this.label = label; return this; }
    /** 섹션 하단의 보조 설명을 설정한다. */
    public SectionBuilder supportingText(String text) { this.supportingText = text; return this; }
    /** 입력 필드의 플레이스홀더(힌트)를 설정한다. */
    public SectionBuilder placeholder(String placeholder) { this.placeholder = placeholder; return this; }
    /** 섹션 컨테이너에 적용할 추가 CSS 클래스를 설정한다. */
    public SectionBuilder cssClass(String cssClass) { this.cssClass = cssClass; return this; }

    /**
     * 최종 SectionElement를 생성하고 대응하는 Presenter를 초기화한다.
     * @param modeName 섹션이 다루는 상태 모드 (CreateWorkspaceMode.Mode.name())
     */
    public SectionElement build(String modeName) {
        SectionElement element = factory.create(modeName);
        if (label != null) element.label(label);
        if (supportingText != null) element.supportingText(supportingText);
        if (placeholder != null) element.placeholder(placeholder);
        if (cssClass != null) element.element().classList.add(cssClass);
        
        // Presenter를 생성하여 상태 구독 및 이벤트 바인딩 활성화 (인스턴스는 내부적으로 유지됨)
        presenterFactory.create(element, modeName);
        
        return element;
    }
}

