package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelProxySetError {
    TunnelNotFound("TunnelNotFound"),
    ProxyProtocolNotSupportedByAgent("ProxyProtocolNotSupportedByAgent");

    private final String value;

    TunnelProxySetError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
