package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HostnameVerifyLevel {
    None("None"),
    NoRawIp("NoRawIp"),
    NoAutoName("NoAutoName");

    private final String value;

    HostnameVerifyLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
