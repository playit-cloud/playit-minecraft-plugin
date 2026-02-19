package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentRoutingSetError {
    RequiresPremium("RequiresPremium"),
    AgentNotFound("AgentNotFound"),
    InvalidAgentId("InvalidAgentId");

    private final String value;

    AgentRoutingSetError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
