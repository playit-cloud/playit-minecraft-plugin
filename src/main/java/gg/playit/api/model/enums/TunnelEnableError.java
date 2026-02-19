package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelEnableError {
    TunnelNotFound("TunnelNotFound");

    private final String value;

    TunnelEnableError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
