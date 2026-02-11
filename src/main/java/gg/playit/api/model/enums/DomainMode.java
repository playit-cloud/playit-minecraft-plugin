package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DomainMode {
    Ip("Ip"),
    Srv("Srv"),
    SrvAndIp("SrvAndIp"),
    Hostname("Hostname");

    private final String value;

    DomainMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
