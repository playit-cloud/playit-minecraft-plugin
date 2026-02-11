package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PasswordResetError {
    ResetCodeExpired("ResetCodeExpired"),
    InvalidResetCode("InvalidResetCode"),
    InvalidNewPassword("InvalidNewPassword");

    private final String value;

    PasswordResetError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
