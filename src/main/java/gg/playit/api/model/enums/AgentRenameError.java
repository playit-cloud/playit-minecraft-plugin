package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentRenameError {
    AgentNotFound("AgentNotFound"),
    InvalidName("InvalidName"),
    InvalidAgentId("InvalidAgentId");

    private final String value;

    AgentRenameError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
