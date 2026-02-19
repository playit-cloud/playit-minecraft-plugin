package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SigninFail {
    IncorrectCredentials("IncorrectCredentials"),
    AccountBanned("AccountBanned");

    private final String value;

    SigninFail(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
