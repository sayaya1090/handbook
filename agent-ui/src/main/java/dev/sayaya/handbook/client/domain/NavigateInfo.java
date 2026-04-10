package dev.sayaya.handbook.client.domain;

/**
 * 화면 이동 정보.
 * NavigateCommand를 UI에 전달하기 위한 값 객체.
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
