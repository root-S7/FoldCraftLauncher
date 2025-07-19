package com.tungsten.fcl.util;

public enum RuleCheckState {
    NO_CHANGE(0), // 设置项未改变
    SUCCESS(1), // 至少一项被改变
    FAIL(2), // 所有设置项都不成功（若本来就没有规则则算到NO_CHANGE）
    UNKNOWN(3); // 未知错误

    private final int code;

    RuleCheckState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RuleCheckState fromCode(int code) {
        for(RuleCheckState state : values()) {
            if(state.code == code) return state;
        }
        return UNKNOWN;
    }

    public static boolean isNormal(int code) {
        return code < 2;
    }

    public static boolean isNormal(RuleCheckState state) {
        return state != null && state.code < 2;
    }
}