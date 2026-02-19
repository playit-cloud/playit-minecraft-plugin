package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelOfflineReason {
    RequiresPremium("requires-premium"),
    OverPortLimit("over-port-limit"),
    IpUsedInGre("ip-used-in-gre"),
    PublicPortNotAvailable("public-port-not-available");

    private final String value;

    TunnelOfflineReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
