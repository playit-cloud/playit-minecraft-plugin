package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountStatus {
    Guest("guest"),
    EmailNotVerified("email-not-verified"),
    Verified("verified");

    private final String value;

    AccountStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
