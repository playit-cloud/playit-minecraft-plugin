package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProxyProtocol {
    ProxyProtocolV1("proxy-protocol-v1"),
    ProxyProtocolV2("proxy-protocol-v2");

    private final String value;

    ProxyProtocol(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
