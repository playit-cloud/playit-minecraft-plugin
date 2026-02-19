package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ClaimSetupResponse {
    WaitingForUserVisit("WaitingForUserVisit"),
    WaitingForUser("WaitingForUser"),
    UserAccepted("UserAccepted"),
    UserRejected("UserRejected");

    private final String value;

    ClaimSetupResponse(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
