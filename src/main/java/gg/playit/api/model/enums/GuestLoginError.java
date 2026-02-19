package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GuestLoginError {
    AccountIsNotGuest("AccountIsNotGuest");

    private final String value;

    GuestLoginError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
