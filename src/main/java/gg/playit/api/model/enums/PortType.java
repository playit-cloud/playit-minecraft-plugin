package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PortType {
    Tcp("tcp"),
    Udp("udp"),
    Both("both");

    private final String value;

    PortType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
