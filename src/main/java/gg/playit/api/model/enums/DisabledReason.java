package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DisabledReason {
    RequiresPremium("requires-premium"),
    OverPortLimit("over-port-limit");

    private final String value;

    DisabledReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
