package dev.sayaya.handbook.client.domain;

/**
 * 화면 이동 정보 값 객체.
 *
 * <p><b>책임:</b> navigate 커맨드의 메뉴(menu), 도구(tool), URL을 UI에 전달한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 VO)</li></ul></p>
 */
public class NavigateInfo {
    private final String menu;
    private final String tool;
    private final String url;

    public NavigateInfo(String menu, String tool, String url) {
        this.menu = menu;
        this.tool = tool;
        this.url = url;
    }
    public String menu() { return menu; }
    public String tool() { return tool; }
    public String url() { return url; }
}
