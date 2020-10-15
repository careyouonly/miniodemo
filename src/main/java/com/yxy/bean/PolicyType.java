package com.yxy.bean;

public enum PolicyType {
    NONE("none"),
    READ_ONLY("readonly"),
    READ_WRITE("readwrite"),
    WRITE_ONLY("writeonly");

    private final String value;

    private PolicyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
