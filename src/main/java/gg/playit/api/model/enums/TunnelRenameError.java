package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelRenameError {
    TunnelNotFound("TunnelNotFound"),
    NameTooLong("NameTooLong");

    private final String value;

    TunnelRenameError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
