package dev.sayaya.handbook.client.domain;

/** 문서 검색/페이지네이션 상태. */
public class Search {
    public int page;
    public int limit;
    public String sortBy;
    public boolean asc;
    public String type;
    public String serial;
    public Double dateFrom;
    public Double dateTo;

    public static Search defaultSearch() {
        Search s = new Search();
        s.page = 0;
        s.limit = 50;
        s.sortBy = "serial";
        s.asc = true;
        return s;
    }
}
