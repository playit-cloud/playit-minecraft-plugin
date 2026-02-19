package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LoginCreateGuestError {
    Blocked("Blocked");

    private final String value;

    LoginCreateGuestError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
