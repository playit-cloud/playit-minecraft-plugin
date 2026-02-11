package gg.playit.api2.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UpdateError {
    ChangingAgentIdNotAllowed("ChangingAgentIdNotAllowed"),
    TunnelNotFound("TunnelNotFound"),
    CannotUpdateLocalAddressForUnassignedTunnel("CannotUpdateLocalAddressForUnassignedTunnel"),
    InvalidAgentId("InvalidAgentId"),
    AddressOrProxyProtoNotSupportedByAgent("AddressOrProxyProtoNotSupportedByAgent");

    private final String value;

    UpdateError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
