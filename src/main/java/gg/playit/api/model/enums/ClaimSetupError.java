package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ClaimSetupError {
    InvalidCode("InvalidCode"),
    CodeExpired("CodeExpired"),
    VersionTextTooLong("VersionTextTooLong");

    private final String value;

    ClaimSetupError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
