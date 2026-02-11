package gg.playit.api2.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentRoutingGetError {
    MissingAgentId("MissingAgentId"),
    InvalidAgentId("InvalidAgentId");

    private final String value;

    AgentRoutingGetError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
