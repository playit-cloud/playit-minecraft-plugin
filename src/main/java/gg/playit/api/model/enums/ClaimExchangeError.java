package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ClaimExchangeError {
    CodeNotFound("CodeNotFound"),
    CodeExpired("CodeExpired"),
    UserRejected("UserRejected"),
    NotAccepted("NotAccepted"),
    NotSetup("NotSetup");

    private final String value;

    ClaimExchangeError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
