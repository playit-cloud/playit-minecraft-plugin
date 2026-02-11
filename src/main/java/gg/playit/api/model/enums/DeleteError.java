package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DeleteError {
    TunnelNotFound("TunnelNotFound");

    private final String value;

    DeleteError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
