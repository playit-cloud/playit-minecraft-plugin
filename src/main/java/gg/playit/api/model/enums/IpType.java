package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum IpType {
    Both("both"),
    Ip4("ip4"),
    Ip6("ip6");

    private final String value;

    IpType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
