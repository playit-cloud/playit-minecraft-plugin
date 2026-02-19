package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentTunnelDisabled {
    ByUser("ByUser"),
    BySystem("BySystem");

    private final String value;

    AgentTunnelDisabled(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
