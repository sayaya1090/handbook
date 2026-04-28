package dev.sayaya.handbook.domain;

public enum DrawerState {
    COLLAPSE, EXPAND, HIDE,
    /** 모바일: 오버레이 모드. 배경 딤 + position: fixed. 메뉴 선택 시 자동 HIDE. */
    OVERLAY
}
