package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentTunnelAttrType {
    Ip("Ip"),
    Ip4("Ip4"),
    Ip6("Ip6"),
    SockAddr("SockAddr"),
    SockAddr4("SockAddr4"),
    SockAddr6("SockAddr6"),
    Port("Port"),
    U64("U64"),
    I64("I64"),
    Boolean("Boolean"),
    String("String");

    private final String value;

    AgentTunnelAttrType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
