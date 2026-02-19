package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelRatelimitError {
    TunnelNotFound("TunnelNotFound"),
    InvalidRatelimit("InvalidRatelimit"),
    PlayitPremiumRequired("PlayitPremiumRequired");

    private final String value;

    TunnelRatelimitError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
